import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.Repository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SpringCacheMockitoIncompatibilityTest {

    interface MyRepo extends Repository<Object, Long> {

        //@Cacheable(cacheNames = "sample", key="#key.id")
        @Cacheable(cacheNames = "sample", key = "new org.springframework.cache.interceptor.SimpleKey(#key.id)")
        Object findByEmail(MyKey key);
    }

    @AllArgsConstructor
    public static class MyKey{
        long id;
        String name;

        @Override
        public boolean equals(Object obj){
            return id == ((MyKey)obj).id;
        }
    }

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("sample");
        }

        @Bean
        MyRepo myRepo() {
            return Mockito.mock(MyRepo.class);
        }
    }

    @Autowired CacheManager manager;
    @Autowired MyRepo repo;

    @Test
    public void throwsSpelEvaluationException_whenAnyMatcherIsUsedWithObject() {

        Object first = new Object();

        MyKey myKey = new MyKey(1l,"name");

        //both lines below will throws SpelEvaluationException: EL1007E: Property or field 'id' cannot be found on null

        //doReturn(first).when(repo).findByEmail(any(MyKey.class));
        doReturn(first).when(repo).findByEmail(myKey);

        Object result1 = repo.findByEmail(myKey);

        verify(repo,times(1)).findByEmail(myKey);
    }
}