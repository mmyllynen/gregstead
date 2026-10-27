package dev.emi.emi.jemi.impl.extras;

import dev.emi.emi.api.widget.WidgetHolder;

public class JemiWidgetBuilder extends JemiPlaceable<JemiWidgetBuilder> {
   public JemiWidgetBuilder.WidgetConstructor constructor;

   public JemiWidgetBuilder(int width, int height, JemiWidgetBuilder.WidgetConstructor constructor) {
      super(width, height);
      this.constructor = constructor;
   }

   public void addWidgets(WidgetHolder holder) {
      this.constructor.accept(this, holder);
   }

   public interface WidgetConstructor {
      void accept(JemiWidgetBuilder var1, WidgetHolder var2);
   }
}
