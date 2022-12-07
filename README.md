# API Rate Limiting With Bucket4J and Redis

- In this tutorial we will learn how to implement api rate limiting in a scaled service. 
- We will use the [Bucket4J](https://github.com/bucket4j/bucket4j) library to implement it and we will use [Redis](https://redis.io/) as a distributed cache
- Here we can also set diffrent Rate Limit by Users

### Problem with Unlimited Rates

If a public API is allowed its users to make an unlimited number of requests per hour, it could lead to:

- resource exhaustion
- decreasing quality of the service
- denial of service attacks
- This might result in a situation where the service is unavailable or slow. It could also lead to more unexpected costs being incurred by the service.

### How Rate Limiting Helps
Firstly, rate-limiting can prevent denial of service attacks. When coupled with a deduplication mechanism or API keys, rate limiting can also help prevent distributed denial of service attacks.

Secondly, it helps in estimating traffic. This is very important for public APIs. This can also be coupled with automated scripts to monitor and scale the service.

And thirdly, you can use it to implement tier-based pricing. This type of pricing model means that users can pay for a higher rate of requests.

### The Token Bucket Algorithm
Token Bucket is an algorithm that you can use to implement rate limiting. In short, it works as follows:

1. A bucket is created with a certain capacity (number of tokens).
2. When a request comes in, the bucket is checked. If there is enough capacity, the request is allowed to proceed. Otherwise, the request is denied.
3. When a request is allowed, the capacity is reduced.
4. After a certain amount of time, the capacity is replenished.

### How to Implement Token Bucket in a Distributed System
To implement the token bucket algorithm in a distributed system, we need to use a distributed cache.

The cache is a key-value store to store the bucket information. We will use a Redis cache to implement this.

Internally, Bucket4j allows us to plug in any implementation of the Java JCache API. The Redisson client of Redis is the implementation we will use.

### Project Implementation

In this Project i am Using Redis, Mysql, Spring boot etc 

Mysql - here we stored users limit. ex User A is allowed to call 10 API calls in 1 Minute, User B is allowed to call 50 API Calls in 1 Minute

Redis - here we stored Key Values are stored as well as we cached Mysql User Table also.

### Prerequisite

- Mysql should be running 
- create a database with name sample
- execute the script [Create Table Script](https://github.com/kuldeepsingh99/rate-limit/blob/main/src/main/resources/create-table.sql)
- Insert few record with some limit ex. User A with limit 10 , User B with Limit 20 etc
- Redis Instance should be running on 6379 port

### Important Configuration

pom.xml
```
<dependency>
  <groupId>org.redisson</groupId>
  <artifactId>redisson-spring-boot-starter</artifactId>
  <version>3.17.0</version>
</dependency>
<dependency>
  <groupId>com.giffing.bucket4j.spring.boot.starter</groupId>
  <artifactId>bucket4j-spring-boot-starter</artifactId>
  <version>0.5.2</version>
  <exclusions>
    <exclusion>
      <groupId>org.ehcache</groupId>
      <artifactId>ehcache</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```
### Cache Configuration
Firstly, we need to start our Redis server. Let's say we have a Redis server running on port 6379 on our local machine.

- This file creates a configuration object that we can use to create a connection.
- Creates a cache manager using the configuration object. This will internally create a connection to the Redis instance and create a hash called "cache" on it.
- Creates a proxy manager that will be used to access the cache. Whatever our application tries to cache using the JCache API, it will be cached on the Redis instance inside the hash named "cache".
- "userList" is used to cache User Table Data, this is explained in UserService.java

```
@Configuration
public class RedisConfig {

	@Bean
    public Config config() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        return config;
    }
    
    @Bean(name="SpringCM")
    public CacheManager cacheManager(Config config) {
    	CacheManager manager = Caching.getCachingProvider().getCacheManager();
        manager.createCache("cache", RedissonConfiguration.fromConfig(config));
        manager.createCache("userList", RedissonConfiguration.fromConfig(config));
        return manager;
    }
    

    @Bean
    ProxyManager<String> proxyManager(CacheManager cacheManager) {
        return new JCacheProxyManager<>(cacheManager.getCache("cache"));
    }
}
```
### Creating Bucket

- this class creates a bucket (Refill,Bandwidth and Bucket)
- We created the proxy manager for the purpose of storing buckets on Redis. Once a bucket is created, it needs to be cached on Redis and does not need to be created again
-  ProxyManager's takes two parameters – a key and a configuration object that it will use to create the bucket.
-  in getConfigSupplierForUser method it first gets the User Object by UserId and based on the userLimit its creates a Bucket
```
@Service
public class RateLimiter {

	@Autowired
	UserService userService;

	@Autowired
	ProxyManager<String> proxyManager;

	public Bucket resolveBucket(String key) {
		Supplier<BucketConfiguration> configSupplier = getConfigSupplierForUser(key);

		return proxyManager.builder().build(key, configSupplier);
	}

	private Supplier<BucketConfiguration> getConfigSupplierForUser(String userId) {
		User user = userService.getUser(userId);

		Refill refill = Refill.intervally(user.getLimit(), Duration.ofMinutes(1));
		Bandwidth limit = Bandwidth.classic(user.getLimit(), refill);
		return () -> (BucketConfiguration.builder().addLimit(limit).build());

	}
}
```

### API Controller

```
@GetMapping("/v1/user")
	public String getUser() {
		return "Hello Secure User";
	}
	
	@GetMapping("/v2/user")
	public String getUserNotsecure() {
		return "Hello Not Secure User";
	}
```

### How to consume Token

- to make it generic a filter has been created
- all the request URL that start with /v1 are secured with tokens
- user id need to pass in header
- if user exist and and have a valid token, the request will too to controller Layer else it will thror 409 (Too many Request Exception) 
```
@Component
public class RequestFilter extends OncePerRequestFilter {

	@Autowired
	RateLimiter rateLimiter;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if(request.getRequestURI().startsWith("/v1")) {
			String tenantId = request.getHeader("X-Tenant");
			if(StringUtils.isNotBlank(tenantId)) {
				Bucket bucket = rateLimiter.resolveBucket(tenantId);
				if(bucket.tryConsume(1)) {
					filterChain.doFilter(request, response);
				} else {
					sendErrorReponse(response, HttpStatus.TOO_MANY_REQUESTS.value());
				}
			} else {
				sendErrorReponse(response, HttpStatus.FORBIDDEN.value());
			}
		} else {
			filterChain.doFilter(request, response);
		}

	}

	private void sendErrorReponse(HttpServletResponse response, int value) {
		HttpServletResponse resp = (HttpServletResponse)response;
		resp.setStatus(value);
		
		resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
	}

}
```
