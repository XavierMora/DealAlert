package com.games_price_tracker.api.tracking.enqueue_games;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.games_price_tracker.api.tracking.enqueue_games.enums.CancelEnqueueResult;
import com.games_price_tracker.api.tracking.enqueue_games.enums.StartEnqueueResult;

@SpringBootTest
class EnqueueGamesTaskHandlerTest {
    private final EnqueueGamesTaskHandler enqueueHandler;

    @Autowired
    EnqueueGamesTaskHandlerTest(EnqueueGamesTaskHandler enqueueHandler){
        this.enqueueHandler = enqueueHandler;
    }

    @Test
    void shouldStartOnlyOneEnqueue() throws RuntimeException, InterruptedException{
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch wait = new CountDownLatch(2);
        AtomicReferenceArray<StartEnqueueResult> result = new AtomicReferenceArray<>(2);

        for (int i = 0; i < result.length(); i++) {
            final int resultIndex = i;

            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        start.await();
                        result.set(resultIndex, enqueueHandler.start(0));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally{
                        wait.countDown();
                    }
                }
            }).start();
        }

        start.countDown();
        wait.await();
        enqueueHandler.cancel();

        int starts = 0;
        for (int i = 0; i < result.length(); i++) {
            if(result.get(i) == StartEnqueueResult.STARTED) starts++;
        }

        assertEquals(1, starts);
    }

    @Test
    void shouldCancelOnlyOneEnqueue() throws RuntimeException, InterruptedException{
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch wait = new CountDownLatch(2);
        AtomicReferenceArray<CancelEnqueueResult> result = new AtomicReferenceArray<>(2);

        for (int i = 0; i < result.length(); i++) {
            final int resultIndex = i;

            new Thread(new Runnable(){
                @Override
                public void run(){
                    try {
                        start.await();
                        result.set(resultIndex, enqueueHandler.cancel());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally{
                        wait.countDown();
                    }
                }
            }).start();
        }

        enqueueHandler.start(0);
        start.countDown();
        wait.await();

        int cancels = 0;
        for (int i = 0; i < result.length(); i++) {
            if(result.get(i) == CancelEnqueueResult.CANCELED) cancels++;
        }

        assertEquals(1, cancels);
    }
}