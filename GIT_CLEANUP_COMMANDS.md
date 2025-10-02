# Git Repository Cleanup Commands

## Files to Remove from Version Control

Based on the updated `.gitignore`, here are the exact Git commands to clean up your repository:

### 1. Remove IDE-specific files (.idea directory)
```bash
# Remove .idea directory and all its contents from git tracking
git rm -r --cached .idea/
```

### 2. Remove build directories
```bash
# Remove build directories from git tracking
git rm -r --cached build/
git rm -r --cached app/build/
git rm -r --cached */build/
```

### 3. Remove local configuration files
```bash
# Remove local.properties (contains local SDK paths)
git rm --cached local.properties
```

### 4. Remove Gradle cache files
```bash
# Remove .gradle directory
git rm -r --cached .gradle/
```

### 5. Remove generated files and captures
```bash
# Remove captures directory (Android Studio screenshots)
git rm -r --cached captures/
```

### 6. Remove OS-specific files
```bash
# Remove macOS .DS_Store files
git rm --cached .DS_Store
find . -name .DS_Store -exec git rm --cached {} \;

# Remove Windows Thumbs.db files
find . -name Thumbs.db -exec git rm --cached {} \;
```

### 7. Remove Android-specific generated files
```bash
# Remove .iml files (IntelliJ module files)
find . -name "*.iml" -exec git rm --cached {} \;

# Remove external native build
git rm -r --cached .externalNativeBuild/
git rm -r --cached .cxx/
```

### 8. Complete cleanup command sequence
Run these commands in order:

```bash
# 1. Remove all files that should be ignored
git rm -r --cached .idea/ || true
git rm -r --cached build/ || true
git rm -r --cached app/build/ || true
git rm -r --cached .gradle/ || true
git rm --cached local.properties || true
git rm -r --cached captures/ || true
git rm -r --cached .externalNativeBuild/ || true
git rm -r --cached .cxx/ || true
find . -name "*.iml" -exec git rm --cached {} \; || true
find . -name .DS_Store -exec git rm --cached {} \; || true
find . -name Thumbs.db -exec git rm --cached {} \; || true

# 2. Add the updated .gitignore
git add .gitignore

# 3. Commit the cleanup
git commit -m "🧹 Clean up repository: update .gitignore and remove unwanted files

- Updated .gitignore with comprehensive Android project exclusions
- Removed IDE-specific files (.idea/, *.iml)
- Removed build directories and cache files
- Removed local configuration files
- Removed OS-specific files (.DS_Store, Thumbs.db)
- Repository is now clean and follows Android best practices"

# 4. Push the changes
git push
```

### 9. Verify cleanup
After running the commands, verify the cleanup:

```bash
# Check what files are currently tracked
git ls-files

# Check repository status
git status

# Verify .gitignore is working
git check-ignore build/
git check-ignore .idea/
```

## Notes

- The `|| true` additions prevent the script from failing if a file/directory doesn't exist
- Some files might not exist in your repository, which is fine
- After cleanup, these files will no longer be tracked by Git
- Local copies will remain on your machine but won't be committed to version control
- Future builds will create these files locally but Git will ignore them

## Repository Size Reduction

After this cleanup, you can also reduce repository size by cleaning Git history:

```bash
# Clean up Git's internal cache
git gc --prune=now --aggressive

# Force push to update remote (use with caution)
# git push --force-with-lease
```

**Important:** Only use `--force-with-lease` if you're the only contributor or have coordinated with your team.