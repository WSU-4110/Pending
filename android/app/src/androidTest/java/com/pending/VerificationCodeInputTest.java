package com.pending;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pending.ui.VerificationCodeInput;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VerificationCodeInputTest {
    @Test
    public void nativePasteAndKeyboardEditingKeepOneCodeValue() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context context = new ContextThemeWrapper(
                    InstrumentationRegistry.getInstrumentation().getTargetContext(), R.style.Theme_Pending);
            VerificationCodeInput input = android.view.LayoutInflater.from(context)
                    .inflate(R.layout.verify_email, null).findViewById(R.id.editInboxCodeNumberSigned);
            EditorInfo info = new EditorInfo();
            InputConnection keyboard = input.onCreateInputConnection(info);
            assertEquals(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD, info.inputType);
            assertTrue((info.imeOptions & EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0);
            assertEquals(null, input.getTransformationMethod());
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                assertArrayEquals(new String[]{"emailOTPCode", "smsOTPCode"}, input.getAutofillHints());
                assertEquals(android.view.View.IMPORTANT_FOR_AUTOFILL_YES, input.getImportantForAutofill());
            }
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Verification test", "123456"));
            assertTrue(input.onTextContextMenuItem(android.R.id.paste));
            assertEquals("123456", input.getText().toString());
            assertEquals(6, input.getSelectionStart());

            keyboard.deleteSurroundingText(1, 0);
            keyboard.commitText("7", 1);
            assertEquals("123457", input.getText().toString());
            input.setSelection(0, 6);
            keyboard.commitText("654321", 1);
            assertEquals("654321", input.getText().toString());
            // The presentation must not introduce a new length-validation rule.
            keyboard.commitText("8", 1);
            assertEquals("6543218", input.getText().toString());
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                input.autofill(android.view.autofill.AutofillValue.forText("246810"));
                assertEquals("246810", input.getText().toString());
            }
            input.setSelection(0, input.length());
            keyboard.commitText("-1.2,3+", 1);
            assertEquals("123", input.getText().toString());
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
        });
    }
}
