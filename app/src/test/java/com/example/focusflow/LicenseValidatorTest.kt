package com.example.focusflow

import com.example.focusflow.services.LicenseValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseValidatorTest {
    @Test fun `builtin code valid`() =
        assertTrue(LicenseValidator.validate("FOCUS-2026-FREE"))

    @Test fun `builtin code valid without dashes`() =
        assertTrue(LicenseValidator.validate("FOCUS2026FREE"))

    @Test fun `builtin code case insensitive`() =
        assertTrue(LicenseValidator.validate("pomodoropro"))

    @Test fun `builtin with spaces`() =
        assertTrue(LicenseValidator.validate("  K0TEU4-PREMIUM  "))

    @Test fun `empty invalid`() = assertFalse(LicenseValidator.validate(""))

    @Test fun `short invalid`() = assertFalse(LicenseValidator.validate("ABCD"))

    @Test fun `wrong checksum invalid`() =
        assertFalse(LicenseValidator.validate("AAAA-BBBB-CCCC-0000"))

    @Test fun `generated code is valid`() {
        val code = LicenseValidator.generateCode("client1")
        assertTrue(LicenseValidator.validate(code))
    }

    @Test fun `generated code deterministic`() {
        assertEquals(LicenseValidator.generateCode("x"), LicenseValidator.generateCode("x"))
    }

    @Test fun `different payload different code`() {
        assertFalse(
            LicenseValidator.generateCode("aaa") == LicenseValidator.generateCode("bbb")
        )
    }
}