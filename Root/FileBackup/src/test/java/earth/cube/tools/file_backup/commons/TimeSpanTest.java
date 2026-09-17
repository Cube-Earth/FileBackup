package earth.cube.tools.file_backup.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TimeSpanTest {

	@Test
	public void test_start_stop_1() throws InterruptedException {
		TimeSpan span = new TimeSpan();
		span.start();
		Thread.sleep(500);
		span.stop();
		System.out.println(span.getElapsedTime());
		assertTrue(span.getElapsedTime() >= 500);
		assertTrue(span.getElapsedTime() <= 512);
	}
	
	@Test
	public void test_elapsed_1() throws InterruptedException {
		TimeSpan span = new TimeSpan();
		span.start(TimeSpan.getTime(2023, 0, 1, 12, 30, 15, 500));
		span.stop(TimeSpan.getTime(2024, 2, 6, 13, 45, 50, 900));
		assertEquals((365+(31+29)+5)*1000*60*60*24L + 1*1000*60*60L + 15*1000*60L + 35*1000 + 400, span.getElapsedTime());
		assertEquals("1y 2M 5d", span.getElapsedTimeAsString());
	}

	@Test
	public void test_elapsed_3() throws InterruptedException {
		TimeSpan span = new TimeSpan();
		span.start(TimeSpan.getTime(2023, 0, 1, 12, 30, 15, 500));
		span.stop(TimeSpan.getTime(2023, 0, 1, 12, 30, 15, 500));
		assertEquals(0, span.getElapsedTime());
		assertEquals("0S", span.getElapsedTimeAsString());
	}

	@Test
	public void test_due_1() throws InterruptedException {
		TimeSpan span = new TimeSpan("1y 2M 5d 1h 15m 35s 400S");
		span.start(TimeSpan.getTime(2023, 0, 1, 12, 30, 15, 500));
		
		assertEquals(TimeSpan.getTime(2024, 2, 6, 13, 45, 50, 900), span.getDueTime());
	}

	@Test
	public void test_isDue_1() throws InterruptedException {
		TimeSpan span = new TimeSpan("500S");
		span.start();
		Thread.sleep(200);
		assertFalse(span.isDue());
		Thread.sleep(100);
		assertFalse(span.isDue());
		Thread.sleep(200);
		assertTrue(span.isDue());
		Thread.sleep(100);
		assertTrue(span.isDue());
	}
}
