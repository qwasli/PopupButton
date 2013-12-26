package org.vaadin.hene.popupbutton.widgetset.client.ui;

import java.util.Collections;
import java.util.List;

import org.vaadin.hene.popupbutton.PopupButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ConnectorHierarchyChangeEvent.ConnectorHierarchyChangeHandler;
import com.vaadin.client.HasComponentsConnector;
import com.vaadin.client.VCaption;
import com.vaadin.client.VCaptionWrapper;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(PopupButton.class)
public class PopupButtonConnector extends ButtonConnector implements
		HasComponentsConnector, ConnectorHierarchyChangeHandler,CloseHandler<PopupPanel> {

	private PopupButtonServerRpc rpc = RpcProxy.create(
			PopupButtonServerRpc.class, this);

	private HandlerRegistration nativePreviewHandler;

	public PopupButtonConnector() {
		addConnectorHierarchyChangeHandler(this);
	}

	@Override
	public void init() {
		super.init();
		getWidget().popup.addCloseHandler(this);
	}

	@Override
	public void onUnregister() {
		super.onUnregister();
		if (nativePreviewHandler != null) {
			nativePreviewHandler.removeHandler();
			nativePreviewHandler = null;
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		if (!getState().popupVisible && isEnabled()) {
			rpc.setPopupVisible(true);
		} else if (isEnabled()) {
			getWidget().popup.hide();
		}
		super.onClick(event);
	}

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		getWidget().addStyleName(VPopupButton.CLASSNAME);
	}

	@Override
	public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
		if (getChildComponents().isEmpty()) {
            getWidget().hidePopup();
			getWidget().popup.setWidget(null);
		} else {
			getWidget().popup.setVisible(false);
			getWidget().popup.show();
			getWidget().popup.setWidget(childrenComponentConnector.getWidget());
            getWidget().setPopupStyleNames(getState().styles);
            getWidget().showPopup();
		}
	}

	@Override
	protected VPopupButton createWidget() {
		return GWT.create(VPopupButton.class);
	}

	@Override
	public VPopupButton getWidget() {
		return (VPopupButton) super.getWidget();
	}

	@Override
	public PopupButtonState getState() {
		return (PopupButtonState) super.getState();
	}

	@Override
	public void updateCaption(ComponentConnector component) {
		if (VCaption.isNeeded(component.getState())) {
			if (getWidget().popup.getCaptionWrapper() != null) {
				getWidget().popup.getCaptionWrapper().updateCaption();
			} else {
				VCaptionWrapper captionWrapper = new VCaptionWrapper(component,
						getConnection());
				getWidget().popup.setWidget(captionWrapper);
				captionWrapper.updateCaption();
			}
		} else {
			if (getWidget().popup.getCaptionWrapper() != null) {
				getWidget().popup.setWidget(getWidget().popup
						.getCaptionWrapper().getWrappedConnector().getWidget());
			}
		}
	}

	private ComponentConnector childrenComponentConnector;

	@Override
	public List<ComponentConnector> getChildComponents() {
		if (childrenComponentConnector == null) {
			return Collections.<ComponentConnector> emptyList();
		}
		return Collections.singletonList(childrenComponentConnector);
	}

	@Override
	public void setChildComponents(List<ComponentConnector> children) {
		if (children.size() > 1) {
			throw new IllegalArgumentException("");
		}

		if (!children.isEmpty()) {
			childrenComponentConnector = children.get(0);
		} else {
			childrenComponentConnector = null;
		}
	}

	@Override
	public HandlerRegistration addConnectorHierarchyChangeHandler(
			ConnectorHierarchyChangeHandler handler) {
		return ensureHandlerManager().addHandler(
				ConnectorHierarchyChangeEvent.TYPE, handler);
	}

	public void onPreviewNativeEvent(NativePreviewEvent event) {
		if (isEnabled()) {
			Element target = Element
					.as(event.getNativeEvent().getEventTarget());
			switch (event.getTypeInt()) {
			case Event.ONCLICK:
				if (getWidget().isOrHasChildOfButton(target)) {
					if (getState().popupVisible) {
                        getWidget().sync();
						rpc.setPopupVisible(false);
					}
				}
				break;
			case Event.ONMOUSEDOWN:
				if (!getWidget().isOrHasChildOfPopup(target)
						&& !getWidget().isOrHasChildOfConsole(target)
						&& !getWidget().isOrHasChildOfButton(target)) {
					if (getState().popupVisible) {
                        getWidget().sync();
						rpc.setPopupVisible(false);
					}
				}
				break;
			case Event.ONKEYPRESS:
				if (getWidget().isOrHasChildOfPopup(target)) {
                    // Catch children that use keyboard, so we can unfocus
                    // them
                    // when
                    // hiding
                    getWidget().addToActiveChildren(target);
                }
				break;
            case Event.ONKEYDOWN:
                if (getState().popupVisible) {
                    getWidget().onKeyDownOnVisiblePopup(event.getNativeEvent(), this);
                }
                break;
			default:
				break;
			}
		}
	}

	@Override
	public boolean delegateCaptionHandling() {
		return false;
	}

	@Override
	public void onClose(CloseEvent<PopupPanel> event) {
		rpc.setPopupVisible(false);
	}

}
