import os
import re

command_dir = "atlaspay-identity/src/main/java/com/atlaspay/identity/application/command"
usecase_dir = "atlaspay-identity/src/main/java/com/atlaspay/identity/application/usecase"

# 1. Update package in all Command files
for filename in os.listdir(command_dir):
    if not filename.endswith("Command.java"):
        continue
    path = os.path.join(command_dir, filename)
    with open(path, "r") as f:
        content = f.read()
    
    content = content.replace("package com.atlaspay.identity.application.usecase;", "package com.atlaspay.identity.application.command;")
    
    with open(path, "w") as f:
        f.write(content)

# 2. Add imports in all UseCase files
for filename in os.listdir(usecase_dir):
    if not filename.endswith("UseCase.java"):
        continue
    path = os.path.join(usecase_dir, filename)
    with open(path, "r") as f:
        content = f.read()
    
    # Check if this usecase uses a command
    match = re.search(r"execute\s*\(([A-Za-z0-9_]+Command)\s+[a-zA-Z0-9_]+\)", content)
    if match:
        command_class = match.group(1)
        if "import com.atlaspay.identity.application.command." + command_class not in content:
            content = re.sub(
                r"package com.atlaspay.identity.application.usecase;\n",
                f"package com.atlaspay.identity.application.usecase;\n\nimport com.atlaspay.identity.application.command.{command_class};\n",
                content
            )
            with open(path, "w") as f:
                f.write(content)
