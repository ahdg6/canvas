package grondag.canvas.config.builder;

import java.util.function.Consumer;
import java.util.function.Supplier;

import grondag.canvas.config.gui.ListItem;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class OptionItem<T> extends ListItem implements Option {
	private static final int RESET_BUTTON_WIDTH = 48;
	private static final Component RESET = new TranslatableComponent("config.canvas.reset");
	private Button resetButton;

	protected final Supplier<T> getter;
	protected final Consumer<T> setter;
	protected final T defaultVal;
	protected final String label;

	public OptionItem(String key, Supplier<T> getter, Consumer<T> setter, T defaultVal, @Nullable String tooltipKey) {
		super(key, tooltipKey);
		this.getter = getter;
		this.setter = setter;
		this.defaultVal = defaultVal;
		label = I18n.get(key);
	}

	protected TextComponent label(String value) {
		return new TextComponent(string(value));
	}

	protected String string(String value) {
		return label + ": §e" + value;
	}

	protected TextComponent label() {
		return new TextComponent(label);
	}

	@Override
	public final void refreshResetButton() {
		resetButton.active = resetActive();
	}

	protected final void createWidget(int x, int y, int width, int height) {
		resetButton = new Button(x + width - RESET_BUTTON_WIDTH, y, RESET_BUTTON_WIDTH, height, RESET, this::doReset);
		createSetterWidget(x, y, width - RESET_BUTTON_WIDTH, height);
		add(resetButton);
		refreshResetButton();
	}

	protected abstract void doReset(AbstractButton button);

	protected abstract void createSetterWidget(int x, int y, int width, int height);

	protected boolean resetActive() {
		return !defaultVal.equals(getter.get());
	}

	@Override
	public ListItem listItem() {
		return this;
	}
}

