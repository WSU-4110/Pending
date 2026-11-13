package com.pending;

import static org.junit.Assert.assertEquals;
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
            VerificationCodeInput input = new VerificationCodeInput(context, null);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Verification test", "123456"));
            assertTrue(input.onTextContextMenuItem(android.R.id.paste));
            assertEquals("123456", input.getText().toString());
            assertEquals(6, input.getSelectionStart());

            InputConnection keyboard = input.onCreateInputConnection(new EditorInfo());
            keyboard.deleteSurroundingText(1, 0);
            keyboard.commitText("7", 1);
            assertEquals("123457", input.getText().toString());
            input.setSelection(0, 6);
            keyboard.commitText("654321", 1);
            assertEquals("654321", input.getText().toString());
            // The presentation must not introduce a new length-validation rule.
            keyboard.commitText("8", 1);
            assertEquals("6543218", input.getText().toString());
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
        });
    }
}
