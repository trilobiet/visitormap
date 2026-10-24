package com.trilobiet.visitorstats.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.RedisClient;

public class RedisDaoImplTest {

	private RedisClient redisClientMock;
	private RedisDaoImpl redisDaoImpl;

	@BeforeEach
	void setUp() {
		redisClientMock = mock(RedisClient.class);
		redisDaoImpl = new RedisDaoImpl(redisClientMock);
	}

	@Test
	void testGetKeys() {

		Set<String> mockKeys = new HashSet<>();
		mockKeys.add("xyz:visits:/page/whatever:2026-10-22");
		mockKeys.add("xyz:visits:/page/whatever:2026-10-23");
		mockKeys.add("xyz:visits:/page/whatelse:2026-10-22");

		when(redisClientMock.keys("*:visits:*")).thenReturn(mockKeys);

		Set<String> returnedKeys = redisDaoImpl.getKeys();

		// System.out.println(returnedKeys);

		assertEquals(2, returnedKeys.size());
		assertTrue(returnedKeys.contains("xyz:visits:/page/whatever"));
		assertTrue(returnedKeys.contains("xyz:visits:/page/whatelse"));
	}

	@Test
	void testGetDates() {

		Set<String> mockKeys = new HashSet<>();
		mockKeys.add("xyz:visits:all:2026-10-22");
		mockKeys.add("xyz:visits:all:2026-10-23");
		mockKeys.add("xyz:visits:all:2026-10-23");

		when(redisClientMock.keys("*:visits:all:*")).thenReturn(mockKeys);

		Set<String> returnedDates = redisDaoImpl.getDates();

		// System.out.println(returnedDates);

		assertEquals(2, returnedDates.size());
		assertTrue(returnedDates.contains("2026-10-22"));
		assertTrue(returnedDates.contains("2026-10-23"));
	}
}
