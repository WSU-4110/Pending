package com.pending.dto;

public record AuthResponse(

   Long userId,
   String email,
   String firstName,
   String lastName,
   String token,
   String message
) {}
