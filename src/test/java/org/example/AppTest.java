package org.example;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    private List<String> rawStrings;
    private Flux  publisher;
    @BeforeEach
    void init(){
        this.rawStrings= Arrays.asList("How","WE","Know","this","Works");
        this.publisher=Flux.fromIterable(rawStrings);

    }

    @Test
    public void verifyJunitRuns(){
        rawStrings.forEach(System.out::println);
    }
    @Test
    void singleOptionValuePublisher(){
        Mono<String> pubOne=Mono.justOrEmpty("Just processing Single value publish");
        pubOne.subscribe(System.out::println);

    }
    @Test
    void simpleSubscription(){
        Flux.fromIterable(rawStrings).subscribe(System.out::println);
    }
    @Test
    void simpleSubscriptionEvent(){
        publisher.subscribe(
                n-> System.out.println(n),
                error-> System.out.println("received an error::"+error),
                ()-> System.out.println("received on complete")

        );
    }
    @Test
    void moreThanOneSubscriber(){
        Flux<Integer> intPub=Flux.range(1,20);
        intPub.log().subscribe(n-> System.out.printf("first subscription :: %s%n",n));
        intPub.subscribe(n-> System.out.printf("second subscription:: %s%n",n));
    }
    @Test
    void fetchFromStreamSubscriber(){
        Flux<String> stSub=Flux.fromStream(rawStrings.stream());
        stSub.subscribe(n-> System.out.printf("first subscription :: %s%n",n));
        //stSub.subscribe(n-> System.out.printf("second subscription:: %s%n",n));
    }

    @Test
    void streamWithConnectableSubscriber(){
        ConnectableFlux<String> conSub=Flux.fromStream(rawStrings.stream()).publish();

        conSub.subscribe(n-> System.out.printf("first subscription :: %s%n",n));
        conSub.subscribe(n-> System.out.printf("Second subscription :: %s%n",n));
        conSub.subscribe(n-> System.out.printf("Third subscription :: %s%n",n));
        conSub.connect();
    }

    @Test
    void moreThanOneSubscriberWithDelay(){
        Flux<UUID> delayPub=Flux.fromStream(Stream.generate(UUID::randomUUID).limit(2)).log()
                .delayElements(Duration.ofSeconds(1));
        //delayPub.log().doOnNext(n-> System.out.printf("first subscription :: %s%n",n)).blockLast();
        Disposable disposable=delayPub.log().subscribe(n-> System.out.printf("first subscription :: %s%n",n),
                e-> System.out.println("received on error"+e),
                ()-> System.out.println("completed")
                );

        Runnable r=()->{
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            disposable.dispose();
        };
        r.run();
        //intPub.subscribe(n-> System.out.printf("second subscription:: %s%n",n));
    }

    @Test
    void rangeSubscriberWithDelay(){
        Flux<Integer> delayPub=Flux.range(1,10)
                .delayElements(Duration.ofSeconds(1));
        //delayPub.log().doOnNext(n-> System.out.printf("first subscription :: %s%n",n)).blockLast();
       Disposable disposable= delayPub.log().subscribe(n-> System.out.printf("first subscription :: %s%n",n),
                e-> System.out.println("received on error"+e),
                ()-> System.out.println("completed")
        );

        Runnable r=()->{
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            disposable.dispose();
        };
        r.run();
        //intPub.subscribe(n-> System.out.printf("second subscription:: %s%n",n));
    }
    @Test
    void justSubscriberMapTest(){
        Flux<Integer> mapPub=Flux.range(1,10)
                .map(i->i/0).onErrorMap(e->new ArrayIndexOutOfBoundsException("error due to divided by 0"));
        mapPub.log().subscribe(n-> System.out.printf(" subscription received :: %s%n",n),
                e-> System.out.println("received on error"+e),
                ()-> System.out.println("completed")
        );
    }
}
