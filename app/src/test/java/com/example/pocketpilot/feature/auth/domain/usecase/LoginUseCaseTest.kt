package com.example.pocketpilot.feature.auth.domain.usecase

import com.example.pocketpilot.feature.auth.domain.model.AuthException
import com.example.pocketpilot.feature.auth.domain.model.User
import com.example.pocketpilot.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class LoginUseCaseTest {

    private val repository: AuthRepository = mockk()
    private val useCase = LoginUseCase(repository)

    @Test
    fun `trims the email before delegating to repository`() = runTest {
        val user = User(id = "u", email = "user@example.com", displayName = "User")
        coEvery { repository.login("user@example.com", "secret12") } returns user

        val result = useCase("  user@example.com  ", "secret12")

        assertEquals(user, result)
        coVerify(exactly = 1) { repository.login("user@example.com", "secret12") }
    }

    @Test
    fun `propagates auth exceptions from the repository`() = runTest {
        coEvery { repository.login(any(), any()) } throws AuthException.InvalidCredentials

        try {
            useCase("user@example.com", "wrong")
            fail("expected AuthException.InvalidCredentials")
        } catch (t: AuthException.InvalidCredentials) {
            // expected
        }
    }
}
