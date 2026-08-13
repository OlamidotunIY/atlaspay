local currentKey = KEYS[1]
local previousKey = KEYS[2]

local windowSize = tonumber(ARGV[1])
local maxRequests = tonumber(ARGV[2])
local nowMs = tonumber(ARGV[3]) 
local currentWindowStartMs = tonumber(ARGV[4]) 

local prevCountStr = redis.call('GET', previousKey)
local prevCount = 0
if prevCountStr ~= false then
    prevCount = tonumber(prevCountStr)
end

local currCountStr = redis.call('GET', currentKey)
local currCount = 0
if currCountStr ~= false then
    currCount = tonumber(currCountStr)
end

-- Calculate weight of previous window based on how far we are into the current window
local timeIntoCurrentWindow = nowMs - currentWindowStartMs
local windowSizeMs = windowSize * 1000.0
local weight = 1.0 - (timeIntoCurrentWindow / windowSizeMs)
if weight < 0 then weight = 0 end

local estimatedRequests = math.floor((prevCount * weight) + currCount)

local allowed = 0
local remaining = 0
local retryAfter = 0

if estimatedRequests < maxRequests then
    allowed = 1
    currCount = redis.call('INCR', currentKey)
    -- Keep window keys around for exactly 2 full window durations
    redis.call('EXPIRE', currentKey, windowSize * 2)
    remaining = maxRequests - estimatedRequests - 1
    if remaining < 0 then remaining = 0 end
else
    allowed = 0
    remaining = 0
    -- Basic retry-after estimation: Wait until the current window ends, or sliding reduces weight
    retryAfter = math.ceil((windowSizeMs - timeIntoCurrentWindow) / 1000.0)
end

return { allowed, remaining, retryAfter }
