package io.github.zero88.schedulerx.manager;

public final class ManagerExceptions {

    public static class InitSchedulerException extends Exception {

        public InitSchedulerException(Throwable cause) { super(cause); }

    }


    public static class RunSchedulerException extends RuntimeException { }

}
