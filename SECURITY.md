# Security Policy

## Verifying Release Artifacts

Every tagged GitHub Release includes `SHA256SUMS.txt` alongside the APK and AAB. To verify a downloaded artifact:

### PowerShell

```powershell
(Get-FileHash app-release.apk -Algorithm SHA256).Hash
# Compare the output with the corresponding line in SHA256SUMS.txt
```

### Linux / macOS

```bash
sha256sum -c SHA256SUMS.txt
```

### APK Signing Certificate Fingerprint

Verify the APK was signed with the official GuitarTuner release key:

```bash
apksigner verify --print-certs app-release.apk
```

The release signing certificate SHA-256 fingerprint will be published here after the first signed release is created.

## Permissions

GuitarTuner declares a single Android permission:

- `android.permission.RECORD_AUDIO` — used only while the tuner screen is visible.

The app declares **no network permission**. Audio samples are processed locally and are never stored, exported, or uploaded.

## Reporting a Vulnerability

If you find a security issue, please open a GitHub issue or email the maintainer directly. There is no bug bounty, but responsible disclosure is appreciated.
