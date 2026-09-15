# Security audit / remediation notes

## Supplied APK observations
The supplied APK was a native Android OTT application with multiple DEX files, native activities/fragments, ExoPlayer code, and a large collection of third-party advertising, payment, analytics/push, and other SDK code. An additional updater-style package (`androidx/update/app/*`) was also present in the compiled APK. Its presence alone is not proof of malware, but it is unnecessary for this clean rebuild and increases review surface.

No claim is made that every legacy library was malicious. The safer remediation is to avoid carrying unneeded compiled code forward.

## Clean rebuild remediation
This project does **not** copy the original DEX files or legacy SDK binaries. It includes only the code needed for the requested OTT experience:

- AndroidX UI components
- Material Components
- Glide image loading
- AndroidX Media3 playback
- Native API client

The native login/signup flow is absent. Legacy advertising, payment, analytics/push, and updater components are absent.

## Permissions
Only these app permissions are requested:

- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`

The app does not request SMS, contacts, call logs, accessibility service, package installation, overlay, microphone, camera, or storage permissions.

## Network / WebView
- Backend API requests are native HTTPS requests.
- The app is not a WebView wrapper.
- `usesCleartextTraffic=false` is enabled.
- No JavaScript bridge or local-file WebView access exists because the app contains no general-purpose WebView.

## PIN
Playback unlock uses the requested fixed PIN `9966`. Unlock state is stored locally with SharedPreferences. This is an access UX gate, not strong DRM or cryptographic protection; anyone with the source can discover a hard-coded PIN.

## Play Protect
A clean rebuild removes the large legacy SDK/updater attack surface, but no developer can guarantee a particular Play Protect verdict. Use a stable signing key, avoid repackaging unknown binaries, distribute from a trusted channel, and keep dependencies current.
