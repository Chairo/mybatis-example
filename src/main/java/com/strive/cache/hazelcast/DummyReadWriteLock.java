package com.strive.cache.hazelcast;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

class DummyReadWriteLock implements ReadWriteLock {

    private Lock lock = new DummyLock();

    @Override
    public Lock readLock() {
        return this.lock;
    }

    @Override
    public Lock writeLock() {
        return this.lock;
    }

    static class DummyLock implements Lock {

        @Override
        public void lock() {
            // Do Nothing
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            // Do Nothing
        }

        @Override
        public boolean tryLock() {
            return true;
        }

        @Override
        public boolean tryLock(long paramLong, TimeUnit paramTimeUnit) throws InterruptedException {
            return true;
        }

        @Override
        public void unlock() {
            // Do Nothing
        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }
}
