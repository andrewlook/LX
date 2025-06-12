/**
 * This file was generated from ../../../expression.peg
 * See https://canopy.jcoglan.com/ for documentation
 */

package heronarts.lx.structure.expression;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TreeNode implements Iterable<TreeNode> {
    public String text;
    public int offset;
    public List<TreeNode> elements;

    Map<Label, TreeNode> labelled;

    public TreeNode() {
        this("", -1, new ArrayList<TreeNode>(0));
    }

    public TreeNode(String text, int offset, List<TreeNode> elements) {
        this.text = text;
        this.offset = offset;
        this.elements = elements;
        this.labelled = new EnumMap<Label, TreeNode>(Label.class);
    }

    public TreeNode get(Label key) {
        return labelled.get(key);
    }

    public Iterator<TreeNode> iterator() {
        return elements.iterator();
    }
}

class TreeNode1 extends TreeNode {
    TreeNode1(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.TERNARY, elements.get(1));
    }
}

class TreeNode2 extends TreeNode {
    TreeNode2(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.LOGICAL_OR, elements.get(0));
    }
}

class TreeNode3 extends TreeNode {
    TreeNode3(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(6));
        labelled.put(Label.EXPRESSION, elements.get(7));
    }
}

class TreeNode4 extends TreeNode {
    TreeNode4(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.LOGICAL_AND, elements.get(0));
    }
}

class TreeNode5 extends TreeNode {
    TreeNode5(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.LOGICAL_AND, elements.get(3));
    }
}

class TreeNode6 extends TreeNode {
    TreeNode6(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.COMPARISON, elements.get(0));
    }
}

class TreeNode7 extends TreeNode {
    TreeNode7(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.COMPARISON, elements.get(3));
    }
}

class TreeNode8 extends TreeNode {
    TreeNode8(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.ADDITIVE, elements.get(0));
    }
}

class TreeNode9 extends TreeNode {
    TreeNode9(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.ADDITIVE, elements.get(3));
    }
}

class TreeNode10 extends TreeNode {
    TreeNode10(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.MULTIPLICATIVE, elements.get(0));
    }
}

class TreeNode11 extends TreeNode {
    TreeNode11(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.MULTIPLICATIVE, elements.get(3));
    }
}

class TreeNode12 extends TreeNode {
    TreeNode12(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.POWER, elements.get(0));
    }
}

class TreeNode13 extends TreeNode {
    TreeNode13(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.POWER, elements.get(3));
    }
}

class TreeNode14 extends TreeNode {
    TreeNode14(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.UNARY, elements.get(0));
    }
}

class TreeNode15 extends TreeNode {
    TreeNode15(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(2));
        labelled.put(Label.UNARY, elements.get(3));
    }
}

class TreeNode16 extends TreeNode {
    TreeNode16(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(1));
        labelled.put(Label.PRIMARY, elements.get(2));
    }
}

class TreeNode17 extends TreeNode {
    TreeNode17(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.SPC, elements.get(3));
        labelled.put(Label.EXPRESSION, elements.get(2));
    }
}

class TreeNode18 extends TreeNode {
    TreeNode18(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.FUNCTION_NAME, elements.get(0));
        labelled.put(Label.SPC, elements.get(5));
        labelled.put(Label.EXPRESSION, elements.get(4));
    }
}

class TreeNode19 extends TreeNode {
    TreeNode19(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.IDENTIFIER, elements.get(2));
    }
}

class TreeNode20 extends TreeNode {
    TreeNode20(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.IDENTIFIER, elements.get(1));
    }
}

class TreeNode21 extends TreeNode {
    TreeNode21(String text, int offset, List<TreeNode> elements) {
        super(text, offset, elements);
        labelled.put(Label.INTEGER_TYPE, elements.get(0));
    }
}
