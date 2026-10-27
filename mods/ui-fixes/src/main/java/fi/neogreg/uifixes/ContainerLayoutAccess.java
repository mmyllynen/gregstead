package fi.neogreg.uifixes;

/** Screen adapters supply their real panel bounds and refresh the shared IPN layout. */
public interface ContainerLayoutAccess {
    void neogreg$refreshButtonLayout();
    ButtonRails.Rect neogreg$panelBounds();
}
