package org.lean.ui.plugins.perspective.presentation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.ui.hopgui.file.IHopFileType;
import org.apache.hop.ui.hopgui.file.IHopFileTypeHandler;
import org.lean.presentation.LeanPresentation;
import org.lean.ui.core.ConstUi;
import org.lean.ui.leangui.context.IGuiContextHandler;
import org.lean.ui.layout.LeanGuiLayout;
import org.lean.ui.plugins.file.ILeanFileType;
import org.lean.ui.plugins.file.ILeanFileTypeHandler;
import org.lean.ui.plugins.file.presentation.LeanPresentationFileType;
import org.lean.ui.plugins.perspective.LeanPerspectiveBase;
import org.lean.ui.plugins.perspective.ILeanPerspective;
import org.lean.ui.plugins.perspective.LeanPerspectivePlugin;
import org.lean.ui.plugins.perspective.TabItemHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@LeanPerspectivePlugin(
        id = "LeanPresentationPerspective",
        name = "LeanPresentationPerspective",
        description = "Lean Presentation Perspective",
        image = "frontend/images/presentation.svg",
        route = "presentation"
)
@Route(value="presentation", layout = LeanGuiLayout.class)
@GuiPlugin(description = "This perspective allows you to modify a presentation")
public class PresentationPerspective extends LeanPerspectiveBase implements ILeanPerspective{

    private List<TabItemHandler> items;
    private Tabs presentationTabs;
//    private Tab placeholderTab;
    private TabItemHandler activeItem;
    private Div presentationHolderDiv;
    private Map<Tab, PresentationEditor> tabsToPages;

    private final LeanPresentationFileType presentationFileType;

    public PresentationPerspective(){
        super();
        setId("presentation-perspective");

        presentationFileType = new LeanPresentationFileType();

        items = new CopyOnWriteArrayList<>();

        presentationTabs = new Tabs();
        presentationTabs.setHeight(ConstUi.HBAR_HEIGHT);
        presentationTabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        presentationTabs.setWidthFull();

        presentationHolderDiv = new Div();
        presentationHolderDiv.setSizeFull();
        presentationHolderDiv.setId("presentation-tabholder");

        tabsToPages = new HashMap<>();

//        presentationContentDiv = new Div();
//        presentationContentDiv.setSizeFull();
//        presentationContentDiv.setId("presentations-content-div");

//        presentationContentDiv.add(presentationTabs, tabHolderDiv);
        presentationTabs.addSelectedChangeListener(e -> {
            tabsToPages.values().forEach(page -> page.setVisible(false));
            Component selectedPage = tabsToPages.get(presentationTabs.getSelectedTab());
            selectedPage.setVisible(true);
        });

        getContent().add(presentationTabs, presentationHolderDiv);

    }

    @Override
    public String getPluginId() {
        return "presentation-perspective";
    }

    @Override
    public void activate() {
        leanGuiLayout.perspectiveManager.setActivePerspective(this);
    }

    @Override
    public void perspectiveActivated() {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void initialize(LeanGuiLayout leanGuiLayout, IHopMetadataProvider metadataProvider) {
        this.leanGuiLayout = leanGuiLayout;
    }

    @Override
    public ILeanFileTypeHandler getActiveFileTypeHandler() {
        PresentationEditor editor = getActiveEditor();
        if(editor != null){
            return editor;
        }
        return null;
    }

    @Override
    public void setActiveFileTypeHandler(ILeanFileTypeHandler activeFileTypeHandler) {
        if(activeFileTypeHandler instanceof PresentationEditor){
            this.setActiveEditor((PresentationEditor)activeFileTypeHandler);
        }
    }

    @Override
    public List<ILeanFileType> getSupportedLeanFileTypes() {
        return Arrays.asList(presentationFileType);
    }

    @Override
    public boolean remove(ILeanFileTypeHandler typeHandler) {
        return false;
    }

    @Override
    public List<IGuiContextHandler> getContextHandlers() {
        return null;
    }

    public ILeanFileTypeHandler addPresentation(LeanPresentation presentation){

        Tab newTab = new Tab(presentation.getName());

        PresentationEditor presentationEditor = new PresentationEditor(leanGuiLayout, presentation);
        presentationHolderDiv.add(presentationEditor);

        Button closeButton = new Button(VaadinIcon.CLOSE.create(), click -> closePresentation(newTab, presentationEditor));
        newTab.add(closeButton);

        presentationHolderDiv.add(presentationEditor);

        tabsToPages.put(newTab, presentationEditor);
        presentationTabs.add(newTab);
        presentationTabs.setSelectedTab(newTab);

        ContextMenu tabMenu = new ContextMenu();
        tabMenu.setTarget(newTab);
        tabMenu.addItem("Close all tabs", e -> {});
        tabMenu.addItem("Close other tabs", e -> {});
        tabMenu.addItem("Close tabs to the left", e -> {});
        tabMenu.addItem("Close tabs to the right", e -> {});
        tabMenu.addItem("Close tab", e -> closePresentation(newTab, presentationEditor));

        return presentationEditor;
    }

    private void closePresentation(Tab presentationTab, PresentationEditor presentationEditor){
        presentationTabs.remove(presentationTab);
        presentationHolderDiv.remove(presentationEditor);
        tabsToPages.remove(presentationTab, presentationEditor);
    }

    public TabItemHandler findTabItemHandlerWithFilename(String filename){
        if(filename == null){
            return null;
        }
        for(TabItemHandler item : items){
            if(filename.equals(item.getTypeHandler().getFilename())){
                return item;
            }
        }
        return null;
    }

    public void switchToTab(TabItemHandler tabItemHandler){
        presentationTabs.setSelectedTab(tabItemHandler.getTabItem());
        activeItem = tabItemHandler;
    }

    public void setActiveEditor(PresentationEditor editor){
        for(Tab editorTab : tabsToPages.keySet()){
            if(tabsToPages.get(editorTab).equals(editor)){
                presentationTabs.setSelectedTab(editorTab);
            }
        }
    }

    public PresentationEditor getActiveEditor(){
        if(presentationTabs.getSelectedTab() == null){
            return null;
        }
        return tabsToPages.get(presentationTabs.getSelectedTab());
    }
}
