package com.example

import com.example.data.remote.SupabaseClient
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun supabaseUrlSanitization_removesRestAndAuthV1() {
    val expected = "https://spoyjsyzhpvfknflgdqk.supabase.co"

    assertEquals(expected, SupabaseClient.sanitizeSupabaseUrl("https://spoyjsyzhpvfknflgdqk.supabase.co/rest/v1/"))
    assertEquals(expected, SupabaseClient.sanitizeSupabaseUrl("https://spoyjsyzhpvfknflgdqk.supabase.co/rest/v1"))
    assertEquals(expected, SupabaseClient.sanitizeSupabaseUrl("https://spoyjsyzhpvfknflgdqk.supabase.co/auth/v1/"))
    assertEquals(expected, SupabaseClient.sanitizeSupabaseUrl("https://spoyjsyzhpvfknflgdqk.supabase.co/"))
    assertEquals(expected, SupabaseClient.sanitizeSupabaseUrl("https://spoyjsyzhpvfknflgdqk.supabase.co"))
    assertEquals(expected, SupabaseClient.sanitizeSupabaseUrl("  https://spoyjsyzhpvfknflgdqk.supabase.co/rest/v1/  "))
  }
}
