import os
import re

use_cases_dir = "atlaspay-identity/src/main/java/com/atlaspay/identity/application/usecase"

for filename in os.listdir(use_cases_dir):
    if not filename.endswith("UseCase.java"):
        continue
        
    path = os.path.join(use_cases_dir, filename)
    with open(path, "r") as f:
        content = f.read()

    if "extends BaseCommandUseCase" in content:
        # Change BaseCommandUseCase<T> to BaseUseCase<T, Void>
        content = re.sub(r"extends BaseCommandUseCase<([A-Za-z0-9_]+)>", r"extends BaseUseCase<\1, Void>", content)
        
        # Change import
        content = content.replace("import com.atlaspay.shared.usecase.BaseCommandUseCase;", "import com.atlaspay.shared.usecase.BaseUseCase;")
        
        # Change public void execute to public Void execute and add return null
        content = re.sub(r"public void execute\((.*?)\)\s*\{", r"public Void execute(\1) {", content)
        
        # Add return null; at the end of the execute method
        # Find the last closing brace before the class's closing brace. 
        # A simple regex for this particular structure:
        content = re.sub(r"(\s+publishEvents\(.*?\);\s*)\}", r"\1\n        return null;\n    }", content)
        
        with open(path, "w") as f:
            f.write(content)
