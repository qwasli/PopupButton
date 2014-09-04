package org.vaadin.hene.popupbutton;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;

import org.vaadin.hene.popupbutton.widgetset.client.ui.PopupButtonServerRpc;
import org.vaadin.hene.popupbutton.widgetset.client.ui.PopupButtonState;

import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.util.ReflectTools;

/**
 * Server side component for the VPopupButton widget.
 */
// This class contains code from AbstractComponentContainer
public class PopupButton extends Button implements SingleComponentContainer {

    private static final long serialVersionUID = -3148268967211155218L;

    private static final Method COMPONENT_ATTACHED_METHOD = ReflectTools
            .findMethod(ComponentAttachListener.class,
                    "componentAttachedToContainer", ComponentAttachEvent.class);

    private static final Method COMPONENT_DETACHED_METHOD = ReflectTools
            .findMethod(ComponentDetachListener.class,
                    "componentDetachedFromContainer",
                    ComponentDetachEvent.class);

    private static final Method POPUP_VISIBILITY_METHOD = ReflectTools
            .findMethod(PopupVisibilityListener.class, "popupVisibilityChange",
                    PopupVisibilityEvent.class);

    private Component component;

    // These can be used by extending PopupButton.
    // It's possible that these are removed in future versions or functionality
    // is changed.
    protected int xOffset = 0;
    protected int yOffset = 0;
    protected boolean popupFixedPosition;
    // protected Paintable popupPositionPaintable; //FIXME

    private PopupButtonServerRpc rpc = new PopupButtonServerRpc() {

        @Override
        public void setPopupVisible(boolean visible) {
            PopupButton.this.setPopupVisible(visible);
        }
    };

    public PopupButton() {
        registerRpc(rpc);
    }

    public PopupButton(String caption) {
        super(caption);
        registerRpc(rpc);
    }

    @Override
    public Iterator<Component> iterator() {
        if (isPopupVisible() && getContent() != null) {
            return Collections.singletonList(component).iterator();
        } else {
            return Collections.<Component> emptyList().iterator();
        }
    }

    /**
     * Shows or hides popup.
     * 
     * @param popupVisible
     *            if true, popup is set to visible, otherwise popup is hidden.
     */
    public void setPopupVisible(boolean popupVisible) {
        if (getState().popupVisible != popupVisible) {
            getState().popupVisible = popupVisible;

            if (component == null) {
                return;
            }

            if (popupVisible) {
                if (component.getParent() != null
                        && component.getParent() != this) {
                    // If the component already has a parent, try to remove it
                    AbstractSingleComponentContainer
                            .removeFromParent(component);
                }
                component.setParent(this);
            } else {
                if (component.getParent() == this) {
                    component.setParent(null);
                }
            }
            fireEvent(new PopupVisibilityEvent(this));
            markAsDirty();
        }
    }

    /**
     * Checks if the popup is visible.
     * 
     * @return true, if popup is visible, false otherwise.
     */
    public boolean isPopupVisible() {
        return getState().popupVisible;
    }

    /**
     * 
     * Allows setting the auto hide behaviour of the popup.
     * 
     * @param popupAutoHide
     *            if true, popup will hide automatically if clicked outside
     *            otherwise it must be closed by clicking on the button or
     *            programmatically.
     */
    public void setPopupAutoHide(boolean popupAutoHide) {
        if (getState().popupAutoHide != popupAutoHide) {
            getState().popupAutoHide = popupAutoHide;
            markAsDirty();
        }
    }

    /**
     * Checks if auto hide behaviour is enabled.
     * 
     * @return true, if auto hide is enabled, false otherwise
     */
    public boolean isPopupAutoHide() {
        return getState().popupAutoHide;
    }

    /**
     * Set the content component of the popup.
     * 
     * @param component
     *            the component to be displayed in the popup.
     * @deprecated Use {@link setContent(Component content)} instead
     */
    @Deprecated
    public void setComponent(Component component) {
        setContent(component);
    }

    @Override
    public Component getContent() {
        return component;
    }

    @Override
    public void setContent(Component content) {
        component = content;
        markAsDirty();
    }

    /**
     * Add a listener that is called whenever the visibility of the popup is
     * changed.
     * 
     * @param listener
     *            the listener to add
     * @see PopupVisibilityListener
     * @see PopupVisibilityEvent
     * @see #removePopupVisibilityListener(PopupVisibilityListener)
     * 
     */
    public void addPopupVisibilityListener(PopupVisibilityListener listener) {
        addListener(PopupVisibilityEvent.class, listener,
                POPUP_VISIBILITY_METHOD);
    }

    /**
     * Removes a previously added listener, so that it no longer receives events
     * when the visibility of the popup changes.
     * 
     * @param listener
     *            the listener to remove
     * @see PopupVisibilityListener
     * @see #addPopupVisibilityListener(PopupVisibilityListener)
     */
    public void removePopupVisibilityListener(PopupVisibilityListener listener) {
        removeListener(PopupVisibilityEvent.class, listener,
                POPUP_VISIBILITY_METHOD);
    }

    /**
     * This event is received by the PopupVisibilityListeners when the
     * visibility of the popup changes. You can get the new visibility directly
     * with {@link #isPopupVisible()}, or get the PopupButton that produced the
     * event with {@link #getPopupButton()}.
     * 
     */
    public class PopupVisibilityEvent extends Event {

        private static final long serialVersionUID = 3170716121022820317L;

        public PopupVisibilityEvent(PopupButton source) {
            super(source);
        }

        /**
         * Get the PopupButton instance that is the source of this event.
         * 
         * @return the source PopupButton
         */
        public PopupButton getPopupButton() {
            return (PopupButton) getSource();
        }

        /**
         * Returns the current visibility of the popup.
         * 
         * @return true if the popup is visible
         */
        public boolean isPopupVisible() {
            return getPopupButton().isPopupVisible();
        }
    }

    /**
     * Defines a listener that can receive a PopupVisibilityEvent when the
     * visibility of the popup changes.
     * 
     */
    public interface PopupVisibilityListener extends Serializable {
        /**
         * Pass to {@link PopupButton#PopupVisibilityEvent} to start listening
         * for popup visibility changes.
         * 
         * @param event
         *            the event
         * 
         * @see {@link PopupVisibilityEvent}
         * @see {@link PopupButton#addPopupVisibilityListener(PopupVisibilityListener)}
         */
        public void popupVisibilityChange(PopupVisibilityEvent event);
    }

    @Override
    public int getComponentCount() {
        return (isPopupVisible() && getContent() != null ? 1 : 0);
    }

    @Override
    public PopupButtonState getState() {
        return (PopupButtonState) super.getState();
    }

    protected void setPopupPositionComponent(Component component) {
        getState().popupPositionConnector = component;
    }

    @Override
    public void addComponentAttachListener(ComponentAttachListener listener) {
        addListener(ComponentContainer.ComponentAttachEvent.class, listener,
                COMPONENT_ATTACHED_METHOD);
    }

    @Override
    public void removeComponentAttachListener(ComponentAttachListener listener) {
        removeListener(ComponentContainer.ComponentAttachEvent.class, listener,
                COMPONENT_ATTACHED_METHOD);
    }

    @Override
    public void addComponentDetachListener(ComponentDetachListener listener) {
        addListener(ComponentContainer.ComponentDetachEvent.class, listener,
                COMPONENT_DETACHED_METHOD);
    }

    @Override
    public void removeComponentDetachListener(ComponentDetachListener listener) {
        removeListener(ComponentContainer.ComponentDetachEvent.class, listener,
                COMPONENT_DETACHED_METHOD);
    }
}
