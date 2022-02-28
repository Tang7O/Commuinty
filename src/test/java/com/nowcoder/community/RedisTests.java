package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }
    
    @Test
    public void testHash() {
        String redisKey = "test:user";
        
        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","ZhangSan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }
    
    @Test
    public void testList() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);
        redisTemplate.opsForList().leftPush(redisKey,104);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,3));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void TestSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey,"刘备","关羽","张飞","赵云","诸葛亮");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void TestSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey,"小明",80);
        redisTemplate.opsForZSet().add(redisKey,"小红",90);
        redisTemplate.opsForZSet().add(redisKey,"小黑",40);
        redisTemplate.opsForZSet().add(redisKey,"小白",20);
        redisTemplate.opsForZSet().add(redisKey,"小绿",70);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"小红"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"小红"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"小红"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));
    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:students"));

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperations() {

        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    public void testTransactional() {
        Object obj =  redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                // start
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, "zhangsan");
                redisOperations.opsForSet().add(redisKey, "lisi");
                redisOperations.opsForSet().add(redisKey, "wangwu");

                System.out.println(redisOperations.opsForSet().members(redisKey));
                // end
                return redisOperations.exec();
            }
        });

        System.out.println(obj);
    }

}
