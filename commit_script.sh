#!/bin/bash
set -e

# 1. Domain Models
git add atlaspay-identity/src/main/java/com/atlaspay/identity/domain/model/Merchant.java
git commit -m "feat(identity): update Merchant aggregate root with builder and annotations" || true

git add atlaspay-identity/src/main/java/com/atlaspay/identity/domain/model/Customer.java
git commit -m "feat(identity): update Customer entity" || true

git add atlaspay-identity/src/main/java/com/atlaspay/identity/domain/model/SubAccount.java
git commit -m "feat(identity): update SubAccount entity" || true

git add atlaspay-identity/src/main/java/com/atlaspay/identity/domain/model/ApiKey.java
git commit -m "feat(identity): update ApiKey entity" || true

git add atlaspay-identity/src/main/java/com/atlaspay/identity/domain/model/EmailVerificationCode.java
git commit -m "feat(identity): update EmailVerificationCode entity" || true

# 2. Ports
git add atlaspay-identity/src/main/java/com/atlaspay/identity/application/port/out/AccountNameResolutionPort.java
git rm atlaspay-identity/src/main/java/com/atlaspay/identity/application/port/out/AccountResolutionService.java || true
git commit -m "refactor(identity): rename AccountResolutionService to AccountNameResolutionPort" || true

# 3. Use Cases
git add atlaspay-identity/src/main/java/com/atlaspay/identity/application/usecase/CompleteComplianceAccountUseCase.java
git commit -m "feat(identity): implement CompleteComplianceAccountUseCase" || true

git add atlaspay-identity/src/main/java/com/atlaspay/identity/application/usecase/RegisterSubAccountUseCase.java
git commit -m "feat(identity): update RegisterSubAccountUseCase with accurate port name" || true

# 4. Identity Config & Build
git add atlaspay-identity/src/main/java/com/atlaspay/identity/config/IdentityUseCaseConfig.java
git commit -m "feat(identity): register use cases as beans in configuration" || true

git add atlaspay-identity/build.gradle
git commit -m "chore(identity): update build dependencies for identity module" || true

# 5. Identity Infrastructure & Presentation
git add atlaspay-identity/src/main/java/com/atlaspay/identity/infrastructure/
git commit -m "feat(identity): implement infrastructure adapters and JPA repositories" || true

git add atlaspay-identity/src/main/java/com/atlaspay/identity/presentation/
git commit -m "feat(identity): implement REST controllers for identity module" || true

# 6. Shared Kernel Exceptions
git add atlaspay-shared-kernel/src/main/java/com/atlaspay/shared/exception/RateLimitExceededException.java
git commit -m "feat(shared-kernel): add RateLimitExceededException" || true

# 7. Rate Limiter Domain & Ports
git add atlaspay-rate-limiter/build.gradle
git commit -m "chore(rate-limiter): initialize build configuration" || true

git add atlaspay-rate-limiter/src/main/java/com/atlaspay/ratelimiter/domain/
git commit -m "feat(rate-limiter): add core domain models and enums" || true

git add atlaspay-rate-limiter/src/main/java/com/atlaspay/ratelimiter/application/
git commit -m "feat(rate-limiter): add use cases and outbound ports" || true

# 8. Rate Limiter Adapters & Sync
git add atlaspay-rate-limiter/src/main/java/com/atlaspay/ratelimiter/infrastructure/adapter/
git commit -m "feat(rate-limiter): implement Redis token bucket and sliding window adapters" || true

git add atlaspay-rate-limiter/src/main/java/com/atlaspay/ratelimiter/infrastructure/sync/
git commit -m "feat(rate-limiter): add sync worker for dynamic rate limit rules" || true

# 9. Rate Limiter Presentation
git add atlaspay-rate-limiter/src/main/java/com/atlaspay/ratelimiter/presentation/
git add atlaspay-rate-limiter/src/main/java/com/atlaspay/ratelimiter/config/
git commit -m "feat(rate-limiter): implement AOP aspect and global interceptor" || true

# 10. Documentation
git add docs/modules/rate-limiter.md
git commit -m "docs: add design document for rate-limiter module" || true

# 11. AtlasPay App Configurations
git add atlaspay-app/build.gradle
git commit -m "chore(app): link rate-limiter module in root application build" || true

git add atlaspay-app/src/main/resources/
git commit -m "chore(app): update application YML configurations" || true

git add atlaspay-app/src/main/java/com/atlaspay/app/exception/
git commit -m "feat(app): configure GlobalExceptionHandler for rate limits" || true

git add atlaspay-app/src/main/java/com/atlaspay/app/config/
git commit -m "feat(app): configure Swagger OpenAPI and WebMvc interceptors" || true

git add atlaspay-app/src/main/java/com/atlaspay/app/event/
git commit -m "feat(app): add application event listeners" || true

echo "Done committing!"
