import os
import re

use_cases_dir = "atlaspay-identity/src/main/java/com/atlaspay/identity/application/usecase"

for filename in os.listdir(use_cases_dir):
    if not filename.endswith("UseCase.java"):
        continue
        
    path = os.path.join(use_cases_dir, filename)
    with open(path, "r") as f:
        content = f.read()

    # Determine if it's void or returns something
    match = re.search(r"public\s+([A-Za-z0-9_<>]+)\s+execute\s*\(\s*([A-Za-z0-9_]+)\s+([A-Za-z0-9_]+)\s*\)", content)
    if not match:
        continue
    
    return_type = match.group(1)
    input_type = match.group(2)
    
    base_class = ""
    import_base = ""
    if return_type == "void":
        base_class = f"BaseCommandUseCase<{input_type}>"
        import_base = "import com.atlaspay.shared.usecase.BaseCommandUseCase;"
    else:
        base_class = f"BaseUseCase<{input_type}, {return_type}>"
        import_base = "import com.atlaspay.shared.usecase.BaseUseCase;"

    class_name = filename[:-5]
    
    # 1. Update class declaration
    content = re.sub(
        r"public\s+class\s+" + class_name + r"\s*\{",
        f"public class {class_name} extends {base_class} {{",
        content
    )
    
    # 2. Add import
    content = re.sub(
        r"package com.atlaspay.identity.application.usecase;\n",
        f"package com.atlaspay.identity.application.usecase;\n\n{import_base}\n",
        content
    )
    
    # 3. Add @Override to execute
    content = re.sub(
        r"public\s+" + return_type + r"\s+execute\s*\(\s*" + input_type + r"\s+([A-Za-z0-9_]+)\s*\)\s*\{",
        f"@Override\n    public {return_type} execute({input_type} \\1) {{",
        content
    )
    
    # 4. Remove private publishEvent method
    content = re.sub(
        r"\s+private\s+<T>\s+void\s+publishEvent\(DomainEvent<T>\s+event\)\s*\{\s*eventPublisher\.publish\(EnvelopedDomainEvent\.wrap\(event\)\);\s*\}\n*",
        "\n",
        content
    )

    # 5. Remove DomainEvent, DomainEventPublisher, EnvelopedDomainEvent imports if they are unused
    # Actually DomainEventPublisher is used in constructor, keep it. EnvelopedDomainEvent and DomainEvent can be removed.
    content = re.sub(r"import com\.atlaspay\.shared\.event\.DomainEvent;\n", "", content)
    content = re.sub(r"import com\.atlaspay\.shared\.event\.EnvelopedDomainEvent;\n", "", content)
    
    # 6. Replace aggregate.pullDomainEvents().forEach(this::publishEvent); with publishEvents(aggregate, eventPublisher);
    content = re.sub(
        r"([a-zA-Z0-9_]+)\.pullDomainEvents\(\)\.forEach\(this::publishEvent\);",
        r"publishEvents(\1, eventPublisher);",
        content
    )
    
    with open(path, "w") as f:
        f.write(content)
