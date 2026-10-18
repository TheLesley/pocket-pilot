# 06 – Auth & app lock

Files: `feature/auth/*`, `feature/security/*`, `MainActivity.kt`

## Auth (sign up, login, reset password)
- Screens: Welcome → Login / SignUp / ForgotPassword / ResetPassword, switched by `AuthNavHost` with an `enum` and `rememberSaveable`.
- ViewModels validate with `AuthValidator` (email format, password rules) before calling use cases.
- **Accounts are stored locally**: `AuthContainer` wires `LocalAuthDataSource`, which saves users in the Room table `auth_accounts`. There's also `FakeAuthRemoteDataSource` (in-memory, demo account) and `RetrofitAuthRemoteDataSource` for a future real API – same interface, swappable.
- Passwords: random 16-byte **salt** + **SHA-256** hash; compared with a **constant-time** check (so timing can't leak how many characters matched).
- The session token lives in `InMemoryAuthTokenStore`, and `MainActivity` tracks `authenticated` with plain `remember` – so **you have to log in every time the app restarts** (see 09).
- Password reset generates a code locally and stores it on the account – there's no email sending.

## App lock (PIN + biometrics)
**`AppLockManager`** is a `DefaultLifecycleObserver` on `ProcessLifecycleOwner` (the whole app, not one screen):
- no PIN/biometric configured → always unlocked
- first time the app comes to the foreground → locked
- later: when the app was in the background longer than the chosen `AutoLockTimeout` → locked
- exposes `state: StateFlow<LockState>`; `AppLockGate` (wrapping everything in `MainActivity`) shows the lock screen when `Locked`.
- Uses `TimeSource.Monotonic` – a clock that can't be fooled by changing the phone's time.

**`BiometricPromptAuthenticator`**:
- `availability()` asks `BiometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)` → available / not enrolled / no hardware…
- `authenticate()` wraps the callback-based `BiometricPrompt` in **`suspendCancellableCoroutine`** so the ViewModel can just `val result = authenticate(...)`. Success/error callbacks `resume` the coroutine; `onAuthenticationFailed` (one bad finger) doesn't, because the dialog stays open.
- `MainActivity` extends **`FragmentActivity`** because `BiometricPrompt` needs one.

**PIN**: `Sha256PinHasher` – random salt + SHA-256. The salt stops pre-computed tables, but a 4–8 digit PIN has at most 100 million combinations, so if someone copied the stored hash they could try them all quickly. Stronger: a slow hash (PBKDF2/Argon2) or Android Keystore-backed encryption. Good thing to raise yourself in an interview.

> ⚠️ The lock can't currently be switched on: the screens that set a PIN or enable biometrics live in Settings, which isn't reachable yet (09).

## Questions
1. Why does `MainActivity` extend `FragmentActivity`?
<details><summary>Show answer</summary>AndroidX `BiometricPrompt` attaches its dialog to a FragmentActivity.</details>

2. What does `suspendCancellableCoroutine` do here?
<details><summary>Show answer</summary>Turns BiometricPrompt's callbacks into a single suspend function that resumes with Success / UserCanceled / Error, and cancels cleanly if the caller is cancelled.</details>

3. How does the app know to lock after being in the background?
<details><summary>Show answer</summary>AppLockManager observes ProcessLifecycleOwner: onStop records a monotonic time mark; onStart checks elapsed time against the auto-lock timeout.</details>

4. What's a salt and why use one?
<details><summary>Show answer</summary>Random data added before hashing so identical passwords produce different hashes and pre-computed rainbow tables don't work.</details>

5. What's weak about a single SHA-256 for a PIN, and what would you use?
<details><summary>Show answer</summary>SHA-256 is fast and the PIN keyspace is tiny, so brute force is trivial offline. Use a slow KDF (PBKDF2, bcrypt, Argon2) and/or Android Keystore, plus attempt limits.</details>

6. Why is there both a Local and a Fake auth data source behind one interface?
<details><summary>Show answer</summary>Dependency inversion: the repository depends on `AuthRemoteDataSource`, so storage can be swapped (local Room, in-memory fake for demos/tests, Retrofit for a real API) without changing anything above it.</details>
