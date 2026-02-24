"""
Migration script to merge all greencoders sub-modules into a single unified project.
All code will live under com.agrifund.<module>.* packages.
"""

import os
import re
import shutil

WORKSPACE = r"c:\Users\brahim\IdeaProjects\agrifund\greencoders"
SRC_MAIN_JAVA = os.path.join(WORKSPACE, "src", "main", "java")
SRC_MAIN_RES = os.path.join(WORKSPACE, "src", "main", "resources")
SRC_TEST_JAVA = os.path.join(WORKSPACE, "src", "test", "java")

# ─── Module configurations ───────────────────────────────────────────────────

MODULES = {
    "chedy": {
        "src_dir": os.path.join(WORKSPACE, "greencoders-chedy", "src", "main", "java"),
        "res_dir": os.path.join(WORKSPACE, "greencoders-chedy", "src", "main", "resources"),
        "test_dir": os.path.join(WORKSPACE, "greencoders-chedy", "src", "test", "java"),
        # Maps old_package -> new_package (ORDER MATTERS: longer/more specific first)
        "package_map": [
            ("controllers", "com.agrifund.chedy.controllers"),
            ("entities",    "com.agrifund.chedy.entities"),
            ("services",    "com.agrifund.chedy.services"),
            ("utils",       "com.agrifund.chedy.utils"),
            ("main",        "com.agrifund.chedy.main"),
            ("test",        "com.agrifund.chedy.test"),
        ],
        # Resource destination subfolder in resources/
        "res_dest": os.path.join("com", "agrifund", "chedy"),
    },
    "rana": {
        "src_dir": os.path.join(WORKSPACE, "greencoders-rana", "src", "main", "java"),
        "res_dir": os.path.join(WORKSPACE, "greencoders-rana", "src", "main", "resources"),
        "test_dir": os.path.join(WORKSPACE, "greencoders-rana", "src", "test", "java"),
        "package_map": [
            ("controlers",  "com.agrifund.rana.controlers"),
            ("entities",    "com.agrifund.rana.entities"),
            ("services",    "com.agrifund.rana.services"),
            ("utils",       "com.agrifund.rana.utils"),
            ("tests",       "com.agrifund.rana.tests"),
            ("org.example", "com.agrifund.rana"),
        ],
        "res_dest": os.path.join("com", "agrifund", "rana"),
    },
    "rayen": {
        "src_dir": os.path.join(WORKSPACE, "greencoders-rayen", "src", "main", "java"),
        "res_dir": os.path.join(WORKSPACE, "greencoders-rayen", "src", "main", "resources"),
        "test_dir": None,
        "package_map": [
            ("controles",   "com.agrifund.rayen.controles"),
            ("entities",    "com.agrifund.rayen.entities"),
            ("services",    "com.agrifund.rayen.services"),
            ("Utils",       "com.agrifund.rayen.utils"),
            ("Tests",       "com.agrifund.rayen.tests"),
        ],
        "res_dest": os.path.join("com", "agrifund", "rayen"),
    },
    "souleima": {
        "src_dir": os.path.join(WORKSPACE, "greencoders-souleima", "src", "main", "java"),
        "res_dir": os.path.join(WORKSPACE, "greencoders-souleima", "src", "main", "resources"),
        "test_dir": os.path.join(WORKSPACE, "greencoders-souleima", "src", "test", "java"),
        "package_map": [
            # More specific sub-packages first
            ("Controllers.admin",        "com.agrifund.souleima.controllers.admin"),
            ("Controllers.agriculteur",  "com.agrifund.souleima.controllers.agriculteur"),
            ("Controllers.banque",       "com.agrifund.souleima.controllers.banque"),
            ("Controllers",              "com.agrifund.souleima.controllers"),
            ("Services",                 "com.agrifund.souleima.services"),
            ("entities",                 "com.agrifund.souleima.entities"),
            ("Utils",                    "com.agrifund.souleima.utils"),
            ("Components",               "com.agrifund.souleima.components"),
            ("Tests",                    "com.agrifund.souleima.tests"),
        ],
        "res_dest": os.path.join("com", "agrifund", "souleima"),
    },
}


def package_to_path(package_name):
    """Convert a.b.c to a/b/c"""
    return package_name.replace(".", os.sep)


def get_all_files(directory, extension=None):
    """Get all files in directory tree, optionally filtered by extension."""
    result = []
    if not directory or not os.path.exists(directory):
        return result
    for root, dirs, files in os.walk(directory):
        for f in files:
            if extension is None or f.endswith(extension):
                result.append(os.path.join(root, f))
    return result


def determine_old_package(filepath, src_dir):
    """Determine the old package from file path relative to src_dir."""
    rel = os.path.relpath(filepath, src_dir)
    parts = rel.replace("/", os.sep).split(os.sep)
    if len(parts) > 1:
        return ".".join(parts[:-1])  # everything except the filename
    return ""  # default package


def rewrite_java_file(content, package_map):
    """
    Rewrite a Java file's package declaration and import statements
    according to the package_map (list of tuples: old -> new).
    """
    # Sort by length of old package descending so longer matches take priority
    sorted_map = sorted(package_map, key=lambda x: len(x[0]), reverse=True)

    # Rewrite package declaration
    def replace_package(match):
        old_pkg = match.group(1)
        for old, new in sorted_map:
            if old_pkg == old or old_pkg.startswith(old + "."):
                suffix = old_pkg[len(old):]
                return f"package {new}{suffix};"
        return match.group(0)

    content = re.sub(r'^(package\s+)([\w.]+);', lambda m: replace_package_full(m, sorted_map), content, count=1, flags=re.MULTILINE)

    # Rewrite imports
    for old, new in sorted_map:
        # Match exact package or sub-packages  
        # e.g., "import Controllers.admin.Foo;" -> "import com.agrifund.souleima.controllers.admin.Foo;"
        # Use word boundary to avoid partial matches
        pattern = r'(import\s+)' + re.escape(old) + r'(\.[A-Za-z_][\w.]*;)'
        replacement = r'\1' + new + r'\2'
        content = re.sub(pattern, replacement, content)

        # Also handle static imports
        pattern = r'(import\s+static\s+)' + re.escape(old) + r'(\.[A-Za-z_][\w.]*;)'
        replacement = r'\1' + new + r'\2'
        content = re.sub(pattern, replacement, content)

    return content


def replace_package_full(match, sorted_map):
    """Replace package declaration."""
    prefix = match.group(1)  # "package "
    old_pkg = match.group(2)
    for old, new in sorted_map:
        if old_pkg == old:
            return f"{prefix}{new};"
        if old_pkg.startswith(old + "."):
            suffix = old_pkg[len(old):]
            return f"{prefix}{new}{suffix};"
    return match.group(0)


def determine_new_package(old_package, package_map):
    """Determine the new package name for a given old package."""
    sorted_map = sorted(package_map, key=lambda x: len(x[0]), reverse=True)
    for old, new in sorted_map:
        if old_package == old:
            return new
        if old_package.startswith(old + "."):
            suffix = old_package[len(old):]
            return new + suffix
    return old_package  # fallback


def rewrite_fxml_file(content, package_map):
    """Rewrite fx:controller references in FXML files."""
    sorted_map = sorted(package_map, key=lambda x: len(x[0]), reverse=True)
    for old, new in sorted_map:
        # Match fx:controller="old.ClassName"
        pattern = r'(fx:controller\s*=\s*")' + re.escape(old) + r'(\.[A-Za-z_][\w.]*")'
        replacement = r'\1' + new + r'\2'
        content = re.sub(pattern, replacement, content)
    return content


def rewrite_resource_paths_in_java(content, old_res_prefix, new_res_prefix):
    """
    Rewrite resource paths like getResource("/Risque.fxml") to 
    getResource("/com/agrifund/chedy/Risque.fxml").
    Also handles stylesheets.
    """
    # Pattern for getResource("/something.fxml") or getResource("/something.css")
    # Only rewrite root-level resources (starting with /)
    def replace_resource(match):
        pre = match.group(1)
        path = match.group(2)
        post = match.group(3)
        # Don't replace if it already has a deep path (e.g., /com/agrifund/...)
        if path.startswith("/com/"):
            return match.group(0)
        # Replace /filename with /new_prefix/filename
        new_path = "/" + new_res_prefix.replace(os.sep, "/") + path
        return pre + new_path + post

    # getResource("/file.fxml"), getResource("/file.css")
    content = re.sub(
        r'(getResource\s*\(\s*")(\/[^"]+\.(fxml|css|html))(")',
        replace_resource,
        content
    )

    return content


def migrate_java_files(module_name, config):
    """Migrate all Java source files for a module."""
    src_dir = config["src_dir"]
    package_map = config["package_map"]
    new_res_prefix = config["res_dest"]

    if not os.path.exists(src_dir):
        print(f"  [SKIP] Source dir not found: {src_dir}")
        return

    java_files = get_all_files(src_dir, ".java")
    print(f"  Found {len(java_files)} Java files in {src_dir}")

    for filepath in java_files:
        old_package = determine_old_package(filepath, src_dir)
        new_package = determine_new_package(old_package, package_map)
        filename = os.path.basename(filepath)

        # Determine destination
        dest_dir = os.path.join(SRC_MAIN_JAVA, package_to_path(new_package))
        dest_file = os.path.join(dest_dir, filename)

        # Read, transform, write
        with open(filepath, "r", encoding="utf-8", errors="replace") as f:
            content = f.read()

        content = rewrite_java_file(content, package_map)
        content = rewrite_resource_paths_in_java(content, "", new_res_prefix)

        os.makedirs(dest_dir, exist_ok=True)
        with open(dest_file, "w", encoding="utf-8") as f:
            f.write(content)

        print(f"    {old_package}.{filename} -> {new_package}.{filename}")


def migrate_test_files(module_name, config):
    """Migrate test files for a module."""
    test_dir = config["test_dir"]
    package_map = config["package_map"]

    if not test_dir or not os.path.exists(test_dir):
        print(f"  [SKIP] No test dir for {module_name}")
        return

    java_files = get_all_files(test_dir, ".java")
    print(f"  Found {len(java_files)} test files")

    for filepath in java_files:
        old_package = determine_old_package(filepath, test_dir)
        if old_package:
            new_package = determine_new_package(old_package, package_map)
        else:
            new_package = f"com.agrifund.{module_name}"
        filename = os.path.basename(filepath)

        dest_dir = os.path.join(SRC_TEST_JAVA, package_to_path(new_package))
        dest_file = os.path.join(dest_dir, filename)

        with open(filepath, "r", encoding="utf-8", errors="replace") as f:
            content = f.read()

        content = rewrite_java_file(content, package_map)

        # Fix test package declaration if it was default package
        if old_package == "":
            # Add/replace package declaration
            if not re.match(r'^\s*package\s+', content):
                content = f"package {new_package};\n\n" + content
            else:
                content = re.sub(r'^(package\s+)[\w.]+;', f'\\1{new_package};', content, count=1, flags=re.MULTILINE)

        os.makedirs(dest_dir, exist_ok=True)
        with open(dest_file, "w", encoding="utf-8") as f:
            f.write(content)

        print(f"    test: {filename} -> {new_package}")


def migrate_resources(module_name, config):
    """Migrate resource files (FXML, CSS, HTML, images) for a module."""
    res_dir = config["res_dir"]
    package_map = config["package_map"]
    res_dest = config["res_dest"]

    if not os.path.exists(res_dir):
        print(f"  [SKIP] Resource dir not found: {res_dir}")
        return

    all_files = get_all_files(res_dir)
    print(f"  Found {len(all_files)} resource files")

    for filepath in all_files:
        rel = os.path.relpath(filepath, res_dir)
        dest_file = os.path.join(SRC_MAIN_RES, res_dest, rel)

        os.makedirs(os.path.dirname(dest_file), exist_ok=True)

        # If it's an FXML file, rewrite controller references
        if filepath.endswith(".fxml"):
            with open(filepath, "r", encoding="utf-8", errors="replace") as f:
                content = f.read()
            content = rewrite_fxml_file(content, package_map)
            
            # Also update resource references within FXML (stylesheets, includes)
            # e.g., @style.css -> @/com/agrifund/chedy/style.css
            # But typically FXML uses relative or @-prefixed paths, let's handle common patterns
            
            with open(dest_file, "w", encoding="utf-8") as f:
                f.write(content)
        else:
            shutil.copy2(filepath, dest_file)

        print(f"    res: {rel} -> {os.path.join(res_dest, rel)}")


def migrate_uploads(module_name):
    """Copy uploads directory if it exists."""
    uploads_src = os.path.join(WORKSPACE, f"greencoders-{module_name}", "uploads")
    uploads_dest = os.path.join(WORKSPACE, "uploads")

    if os.path.exists(uploads_src):
        print(f"  Copying uploads from {uploads_src}")
        if os.path.exists(uploads_dest):
            # Merge
            for root, dirs, files in os.walk(uploads_src):
                rel = os.path.relpath(root, uploads_src)
                dest_dir = os.path.join(uploads_dest, rel)
                os.makedirs(dest_dir, exist_ok=True)
                for f in files:
                    src_file = os.path.join(root, f)
                    dst_file = os.path.join(dest_dir, f)
                    if not os.path.exists(dst_file):
                        shutil.copy2(src_file, dst_file)
        else:
            shutil.copytree(uploads_src, uploads_dest)


def main():
    print("=" * 70)
    print("AgriFund Migration: Merging all modules into single project")
    print("=" * 70)

    for module_name, config in MODULES.items():
        print(f"\n{'─' * 60}")
        print(f"Module: {module_name}")
        print(f"{'─' * 60}")

        print("\n  [1] Migrating Java source files...")
        migrate_java_files(module_name, config)

        print("\n  [2] Migrating test files...")
        migrate_test_files(module_name, config)

        print("\n  [3] Migrating resources...")
        migrate_resources(module_name, config)

        print("\n  [4] Migrating uploads...")
        migrate_uploads(module_name)

    print("\n" + "=" * 70)
    print("Migration complete!")
    print("=" * 70)


if __name__ == "__main__":
    main()
