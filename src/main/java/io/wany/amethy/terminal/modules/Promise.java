package io.wany.amethy.terminal.modules;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Promise {

  /*
   * 
   * return new Promise((resolve, reject) -> {
   * try {
   * // run async task
   * resolve.accept('ok');
   * } catch (Exception e) {
   * reject.accept(e);
   * }
   * });
   * 
   */

  private BiConsumer<Consumer<Object>, Consumer<Object>> task;
  private Consumer<Object> thenConsumer;
  private Consumer<Object> catchConsumer;

  public Promise(BiConsumer<Consumer<Object>, Consumer<Object>> callback) {
    this.task = callback;
  }

  public Promise then(Consumer<Object> callback) {
    this.thenConsumer = callback;
    return this;
  }

  public Promise error(Consumer<Object> callback) {
    this.catchConsumer = callback;
    return this;
  }

  public Promise run() {
    this.task.accept(
        arg0 -> {
          if (thenConsumer != null) {
            thenConsumer.accept(arg0);
          }
        },
        arg1 -> {
          if (catchConsumer != null) {
            catchConsumer.accept(arg1);
          }
        });
    return this;
  }

}
