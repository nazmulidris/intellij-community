// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.ui.customization;

import com.intellij.openapi.actionSystem.*;
import com.intellij.util.ArrayUtil;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomisedActionGroup extends ActionGroup {
  private final ActionGroup myGroup;
  private AnAction[] myChildren;
  private final CustomActionsSchema mySchema;
  private final String myDefaultGroupName;
  private final String myRootGroupName;

  private int mySchemeModificationStamp = -1;
  private boolean myNeedsChildrenRefresh = false;

  public CustomisedActionGroup(String shortName,
                               final ActionGroup group,
                               CustomActionsSchema schema,
                               String defaultGroupName,
                               String name) {
    copyFrom(group);
    getTemplatePresentation().setText(shortName);
    setPopup(group.isPopup());

    myGroup = group;
    mySchema = schema;
    myDefaultGroupName = defaultGroupName;
    myRootGroupName = name;
    myGroup.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        myNeedsChildrenRefresh |= DefaultActionGroup.PROP_CHILDREN_MODIFICATION_COUNT.equals(evt.getPropertyName());
      }
    });
  }

  @Override
  @NotNull
  public AnAction[] getChildren(@Nullable final AnActionEvent e) {
    int currentStamp = CustomActionsSchema.getInstance().getModificationStamp();
    if (mySchemeModificationStamp < currentStamp || myNeedsChildrenRefresh || ArrayUtil.isEmpty(myChildren) ||
        myGroup instanceof DynamicActionGroup || !(myGroup instanceof DefaultActionGroup)) {
      myChildren = CustomizationUtil.getReordableChildren(myGroup, mySchema, myDefaultGroupName, myRootGroupName, e);
      mySchemeModificationStamp = currentStamp;
      myNeedsChildrenRefresh = false;
    }
    return myChildren;
  }

  @Override
  public boolean isPopup() {
    return myGroup.isPopup();
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    myGroup.update(e);
  }

  @Override
  public boolean isDumbAware() {
    return myGroup.isDumbAware();
  }

  @Override
  public boolean canBePerformed(@NotNull DataContext context) {
    return myGroup.canBePerformed(context);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    myGroup.actionPerformed(e);
  }

  @Nullable
  public AnAction getFirstAction() {
    final AnAction[] children = getChildren(null);
    return children.length > 0 ? children[0] : null;
  }

  public ActionGroup getOrigin() { return myGroup; }

  public void resetChildren() {
    myChildren = null;
  }
}
