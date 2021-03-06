package com.scapelog.api.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.scapelog.api.ClientFeature;
import com.scapelog.api.event.Event;
import com.scapelog.api.event.EventListener;
import com.scapelog.api.ui.Overlay;
import com.scapelog.api.ui.tab.BaseTab;
import com.scapelog.api.ui.tab.IconTab;
import com.scapelog.api.util.Components;
import com.scapelog.client.config.ConfigWrapper;
import com.scapelog.client.event.EventDispatcher;
import com.scapelog.client.ui.UserInterface;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Optional;

public abstract class Plugin extends ConfigWrapper {
	private final ImmutableList<ClientFeature> dependingFeatures;
	private final SimpleBooleanProperty status = new SimpleBooleanProperty(false);
	private final List<EventListener<? extends Event>> eventListeners = Lists.newArrayList();
	private final SimpleObjectProperty<GlyphIcons> buttonIconProperty = new SimpleObjectProperty<>(FontAwesomeIcon.QUESTION);

	private boolean started;
	protected BaseTab baseTab;

	private final GlyphIcons icon;
	private final String name;
	private final TabMode tabMode;

	public Plugin(TabMode tabMode, GlyphIcons icon, String name) {
		this(tabMode, icon, name, Optional.<String>empty());
	}

	public Plugin(TabMode tabMode, GlyphIcons icon, String name, Optional<String> configSectionName) {
		this(tabMode, icon, name, configSectionName, new ClientFeature[0]);
	}

	public Plugin(TabMode tabMode, GlyphIcons icon, String name, Optional<String> configSectionName, ClientFeature... dependingFeatures) {
		super(configSectionName);
		this.name = name;
		this.tabMode = tabMode;
		this.icon = icon;
		this.dependingFeatures = ImmutableList.copyOf(dependingFeatures);

		buttonIconProperty.set(icon);
	}

	public final void start() {
		if (started) {
			throw new IllegalStateException("The plugin has already been started");
		}
		started = true;
		status.set(true);
		Platform.runLater(this::onStart);
	}

	public abstract void onStart();

	public abstract Region getContent();

	public abstract OpenTechnique getOpenTechnique();

	public Region getButtonContent() {
		return null;
	}

	public Region getSettingsContent() {
		return null;
	}

	public void onStop() {

	}

	public final BaseTab getInitializedTab() {
		if (baseTab == null) {
			baseTab = new IconTab(icon, name) {
				@Override
				public Node getTabContent() {
					if (hasSettings()) {
						BorderPane content = new BorderPane();
						content.setTop(getHeader());
						content.setCenter(Plugin.this.getContent());
						return content;
					} else {
						return Plugin.this.getContent();
					}
				}
			};
		}
		return baseTab;
	}

	public final ImmutableList<ClientFeature> getDependingFeatures() {
		return dependingFeatures;
	}

	public final SimpleObjectProperty<GlyphIcons> buttonIconPropertyProperty() {
		return buttonIconProperty;
	}

	public final boolean isStarted() {
		return started;
	}

	public final SimpleBooleanProperty statusProperty() {
		return status;
	}

	public final boolean isRunning() {
		return status.get();
	}

	public final String getName() {
		return name;
	}

	public Optional<HBox> getHeaderContent() {
		return Optional.empty();
	}

	protected final Region getHeader() {
		HBox pane = new HBox();
		pane.setId("plugin-header");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button settingsButton = Components.createIconButton(FontAwesomeIcon.COG, "16.0");
		settingsButton.setId("settings");
		settingsButton.setOnAction(e -> {
			BorderPane content = new BorderPane();
			content.setTop(getSettingsHeader(baseTab.getContent()));
			content.setCenter(getSettingsContent());
			baseTab.setContent(content);
		});

		Label label = new Label(name);
		label.setMaxHeight(Double.MAX_VALUE);
		pane.getChildren().addAll(label, spacer);
		getHeaderContent().ifPresent(content -> pane.getChildren().add(content));
		pane.getChildren().add(settingsButton);
		return pane;
	}

	protected final Region getSettingsHeader(Node originalContent) {
		HBox pane = new HBox(3);
		pane.setId("plugin-header");

		Button back = Components.createBorderedButton("Back");
		back.setOnAction(e -> getInitializedTab().setContent(originalContent));

		Label label = new Label(name + " - Settings");
		label.setMaxHeight(Double.MAX_VALUE);
		pane.getChildren().addAll(label, Components.createSpacer(), back);
		return pane;
	}

	protected final void registerListener(EventListener<? extends Event> eventListener) {
		EventDispatcher.registerListener(eventListener);
		eventListeners.add(eventListener);
	}

	public final void showButton() {
		showButton(true);
	}

	public final void hideButton() {
		showButton(false);
	}

	public final void showButton(boolean show) {
		getInitializedTab().show(show);
	}

	public final void stop() {
		status.set(false);
		eventListeners.forEach(EventDispatcher::unregisterListener);
		onStop();
	}

	public final boolean hasTab() {
		return tabMode.equals(TabMode.ON);
	}

	public final void addOverlay(Overlay overlay) {
		UserInterface.addOverlay(overlay);
	}

	public final void addOverlays(Overlay... overlays) {
		UserInterface.addOverlays(overlays);
	}

	public final ObservableList<Overlay> getOverlays() {
		return UserInterface.getOverlays();
	}

	public final void removeOverlay(Overlay overlay) {
		UserInterface.removeOverlay(overlay);
	}

	public final void removeOverlays(Overlay... overlays) {
		UserInterface.removeOverlays(overlays);
	}

}