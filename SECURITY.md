# Security Policy

## Verifying release downloads

Every GitHub release includes a `SHA256SUMS.txt` next to the APK. To check a download:

### PowerShell

```powershell
(Get-FileHash GuitarTuner-vX.Y.Z.apk -Algorithm SHA256).Hash
# Compare the output with the matching line in SHA256SUMS.txt
```

### Linux / macOS

```bash
sha256sum -c SHA256SUMS.txt
```

### APK signing certificate

Check that the APK was signed with the GuitarTuner release key:

```bash
apksigner verify --print-certs GuitarTuner-vX.Y.Z.apk
```

The signer's certificate SHA-256 should be:

```
DB:A1:AA:88:E3:7B:90:15:5F:CA:31:35:CA:3B:78:1D:E9:2C:22:51:07:E4:7C:98:06:E7:5B:F8:80:55:FD:D8
```

If it doesn't match, don't install it.

## Permissions

GuitarTuner asks for one Android permission, `android.permission.RECORD_AUDIO`, and only uses it while the tuner screen is visible.

There's no network permission. Audio is processed on the phone and is never stored, exported or uploaded.

## Reporting a vulnerability

If you find a security issue, please open a GitHub issue or email the maintainer directly. There's no bug bounty, but responsible disclosure is appreciated.
