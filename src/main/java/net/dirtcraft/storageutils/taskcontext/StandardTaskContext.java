/*
 * Copyright (c) 2024 Marc Beckhaeuser (AlphaConqueror) <marcbeckhaeuser@gmail.com>
 *
 * ALL RIGHTS RESERVED.
 */

package net.dirtcraft.storageutils.taskcontext;

import java.util.LinkedList;
import java.util.Queue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.Session;

public class StandardTaskContext implements TaskContext {

    @NonNull
    private final Session session;
    @NonNull
    private final Queue<Runnable> queue = new LinkedList<>();

    public StandardTaskContext(@NonNull final Session session) {
        this.session = session;
    }

    @Override
    public @NonNull Session session() {
        return this.session;
    }

    @Override
    public void queue(@NonNull final Runnable runnable) {
        this.queue.add(runnable);
    }

    @Override
    public void executeTasks() {
        while (!this.queue.isEmpty()) {
            this.queue.poll().run();
        }
    }
}
