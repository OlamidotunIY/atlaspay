local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local bucket = redis.call('HMGET', key, 'tokens', 'lastRefill')
local tokens = tonumber(bucket[1])
local lastRefill = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    lastRefill = now
end

local timePassed = math.max(0, now - lastRefill)
local tokensToAdd = math.floor(timePassed * refillRate)

if tokensToAdd > 0 then
    tokens = math.min(capacity, tokens + tokensToAdd)
    lastRefill = now
end

local allowed = 0
local retryAfter = 0

if tokens >= 1 then
    tokens = tokens - 1
    allowed = 1
    redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)
    
    -- Expire after time needed to refill fully to avoid memory leaks
    local ttl = math.ceil(capacity / refillRate)
    if ttl < 60 then ttl = 60 end -- minimum 60s ttl
    redis.call('EXPIRE', key, ttl)
else
    allowed = 0
    -- Calculate retryAfter (how many seconds until 1 token is available)
    retryAfter = math.max(1, math.ceil(1.0 / refillRate))
    redis.call('HMSET', key, 'tokens', tokens, 'lastRefill', lastRefill)
end

return { allowed, tokens, retryAfter }
