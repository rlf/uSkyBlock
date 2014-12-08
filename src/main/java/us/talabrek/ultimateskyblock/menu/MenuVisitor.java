package us.talabrek.ultimateskyblock.menu;

/**
 */
public interface MenuVisitor {
    void visit(MenuItem menuItem);
    void visit(Menu menu);
}
