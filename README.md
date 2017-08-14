# putty2ssh

Convert your exported list of PuTTY accounts to a ssh confi file.

## Rationale: For those who SSH.

If you're moving from Windows to Linux you'll probably want to keep all your SSH accounts you've been using in PuTTY.

On Linux one uses a `.ssh/config` file to store ssh connections. To speed up the migration convert your list of accounts from PuTTY registery key to a ssh config format.

## Windows: Export Registry Keys

The below command should export all your sessions to the a file `putty.reg` on your desktop.

```
regedit /e "%userprofile%\desktop\putty.reg" HKEY_CURRENT_USER\Software\SimonTatham\PuTTY\Sessions
```

## Linux: Convert and copy to .ssh/config

1. Copy your exported PuTTY sessions to your linux
2. Place the `putty.reg` file in the "./sample" dir relative to the apps working dir
3. compile this repo and run the main function
4. `cp ./sample/ssh_config ~/.ssh/config`

## Links

Converter for Mac-Users using Shuttle:
https://github.com/Dreyer/PuTTY-to-Shuttle-Converter
