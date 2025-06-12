/**
 * This file was generated from ../../../expression.peg
 * See https://canopy.jcoglan.com/ for documentation
 */

package heronarts.lx.structure.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

abstract class Grammar {
    static TreeNode FAILURE = new TreeNode();

    int inputSize, offset, failure;
    String input;
    List<String[]> expected;
    Map<Label, Map<Integer, CacheRecord>> cache;
    Actions actions;

    private static Pattern REGEX_1 = Pattern.compile("\\A[a-zA-Z]");
    private static Pattern REGEX_2 = Pattern.compile("\\A[a-zA-Z0-9]");
    private static Pattern REGEX_3 = Pattern.compile("\\A[0-9]");
    private static Pattern REGEX_4 = Pattern.compile("\\A[0-9]");
    private static Pattern REGEX_5 = Pattern.compile("\\A[ \\t]");

    TreeNode _read_expression() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.expression);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.expression, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            address0 = _read_ternary();
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_ternary() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.ternary);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.ternary, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_logical_or();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                int index3 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>(4);
                TreeNode address3 = FAILURE;
                String chunk0 = null;
                int max0 = offset + 1;
                if (max0 <= inputSize) {
                    chunk0 = input.substring(offset, max0);
                }
                if (chunk0 != null && chunk0.equals("?")) {
                    address3 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address3 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::ternary", "\"?\"" });
                    }
                }
                if (address3 != FAILURE) {
                    elements1.add(0, address3);
                    TreeNode address4 = FAILURE;
                    address4 = _read_expression();
                    if (address4 != FAILURE) {
                        elements1.add(1, address4);
                        TreeNode address5 = FAILURE;
                        String chunk1 = null;
                        int max1 = offset + 1;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && chunk1.equals(":")) {
                            address5 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address5 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::ternary", "\":\"" });
                            }
                        }
                        if (address5 != FAILURE) {
                            elements1.add(2, address5);
                            TreeNode address6 = FAILURE;
                            address6 = _read_expression();
                            if (address6 != FAILURE) {
                                elements1.add(3, address6);
                            } else {
                                elements1 = null;
                                offset = index3;
                            }
                        } else {
                            elements1 = null;
                            offset = index3;
                        }
                    } else {
                        elements1 = null;
                        offset = index3;
                    }
                } else {
                    elements1 = null;
                    offset = index3;
                }
                if (elements1 == null) {
                    address2 = FAILURE;
                } else {
                    address2 = new TreeNode2(input.substring(index3, offset), index3, elements1);
                    offset = offset;
                }
                if (address2 == FAILURE) {
                    address2 = new TreeNode(input.substring(index2, index2), index2, new ArrayList<TreeNode>());
                    offset = index2;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode1(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_logical_or() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.logical_or);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.logical_or, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_logical_and();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    int index3 = offset;
                    List<TreeNode> elements2 = new ArrayList<TreeNode>(2);
                    TreeNode address4 = FAILURE;
                    int index4 = offset;
                    String chunk0 = null;
                    int max0 = offset + 2;
                    if (max0 <= inputSize) {
                        chunk0 = input.substring(offset, max0);
                    }
                    if (chunk0 != null && chunk0.equals("||")) {
                        address4 = new TreeNode(input.substring(offset, offset + 2), offset, new ArrayList<TreeNode>());
                        offset = offset + 2;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::logical_or", "\"||\"" });
                        }
                    }
                    if (address4 == FAILURE) {
                        offset = index4;
                        String chunk1 = null;
                        int max1 = offset + 1;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && chunk1.equals("|")) {
                            address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::logical_or", "\"|\"" });
                            }
                        }
                        if (address4 == FAILURE) {
                            offset = index4;
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_logical_and();
                        if (address5 != FAILURE) {
                            elements2.add(1, address5);
                        } else {
                            elements2 = null;
                            offset = index3;
                        }
                    } else {
                        elements2 = null;
                        offset = index3;
                    }
                    if (elements2 == null) {
                        address3 = FAILURE;
                    } else {
                        address3 = new TreeNode4(input.substring(index3, offset), index3, elements2);
                        offset = offset;
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode3(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_logical_and() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.logical_and);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.logical_and, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_comparison();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    int index3 = offset;
                    List<TreeNode> elements2 = new ArrayList<TreeNode>(2);
                    TreeNode address4 = FAILURE;
                    int index4 = offset;
                    String chunk0 = null;
                    int max0 = offset + 2;
                    if (max0 <= inputSize) {
                        chunk0 = input.substring(offset, max0);
                    }
                    if (chunk0 != null && chunk0.equals("&&")) {
                        address4 = new TreeNode(input.substring(offset, offset + 2), offset, new ArrayList<TreeNode>());
                        offset = offset + 2;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::logical_and", "\"&&\"" });
                        }
                    }
                    if (address4 == FAILURE) {
                        offset = index4;
                        String chunk1 = null;
                        int max1 = offset + 1;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && chunk1.equals("&")) {
                            address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::logical_and", "\"&\"" });
                            }
                        }
                        if (address4 == FAILURE) {
                            offset = index4;
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_comparison();
                        if (address5 != FAILURE) {
                            elements2.add(1, address5);
                        } else {
                            elements2 = null;
                            offset = index3;
                        }
                    } else {
                        elements2 = null;
                        offset = index3;
                    }
                    if (elements2 == null) {
                        address3 = FAILURE;
                    } else {
                        address3 = new TreeNode6(input.substring(index3, offset), index3, elements2);
                        offset = offset;
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode5(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_comparison() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.comparison);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.comparison, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_additive();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    int index3 = offset;
                    List<TreeNode> elements2 = new ArrayList<TreeNode>(2);
                    TreeNode address4 = FAILURE;
                    int index4 = offset;
                    String chunk0 = null;
                    int max0 = offset + 2;
                    if (max0 <= inputSize) {
                        chunk0 = input.substring(offset, max0);
                    }
                    if (chunk0 != null && chunk0.equals("<=")) {
                        address4 = new TreeNode(input.substring(offset, offset + 2), offset, new ArrayList<TreeNode>());
                        offset = offset + 2;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::comparison", "\"<=\"" });
                        }
                    }
                    if (address4 == FAILURE) {
                        offset = index4;
                        String chunk1 = null;
                        int max1 = offset + 2;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && chunk1.equals(">=")) {
                            address4 = new TreeNode(input.substring(offset, offset + 2), offset, new ArrayList<TreeNode>());
                            offset = offset + 2;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::comparison", "\">=\"" });
                            }
                        }
                        if (address4 == FAILURE) {
                            offset = index4;
                            String chunk2 = null;
                            int max2 = offset + 1;
                            if (max2 <= inputSize) {
                                chunk2 = input.substring(offset, max2);
                            }
                            if (chunk2 != null && chunk2.equals("<")) {
                                address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                                offset = offset + 1;
                            } else {
                                address4 = FAILURE;
                                if (offset > failure) {
                                    failure = offset;
                                    expected = new ArrayList<String[]>();
                                }
                                if (offset == failure) {
                                    expected.add(new String[] { "LXFExpression::comparison", "\"<\"" });
                                }
                            }
                            if (address4 == FAILURE) {
                                offset = index4;
                                String chunk3 = null;
                                int max3 = offset + 1;
                                if (max3 <= inputSize) {
                                    chunk3 = input.substring(offset, max3);
                                }
                                if (chunk3 != null && chunk3.equals(">")) {
                                    address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                                    offset = offset + 1;
                                } else {
                                    address4 = FAILURE;
                                    if (offset > failure) {
                                        failure = offset;
                                        expected = new ArrayList<String[]>();
                                    }
                                    if (offset == failure) {
                                        expected.add(new String[] { "LXFExpression::comparison", "\">\"" });
                                    }
                                }
                                if (address4 == FAILURE) {
                                    offset = index4;
                                    String chunk4 = null;
                                    int max4 = offset + 2;
                                    if (max4 <= inputSize) {
                                        chunk4 = input.substring(offset, max4);
                                    }
                                    if (chunk4 != null && chunk4.equals("==")) {
                                        address4 = new TreeNode(input.substring(offset, offset + 2), offset, new ArrayList<TreeNode>());
                                        offset = offset + 2;
                                    } else {
                                        address4 = FAILURE;
                                        if (offset > failure) {
                                            failure = offset;
                                            expected = new ArrayList<String[]>();
                                        }
                                        if (offset == failure) {
                                            expected.add(new String[] { "LXFExpression::comparison", "\"==\"" });
                                        }
                                    }
                                    if (address4 == FAILURE) {
                                        offset = index4;
                                        String chunk5 = null;
                                        int max5 = offset + 2;
                                        if (max5 <= inputSize) {
                                            chunk5 = input.substring(offset, max5);
                                        }
                                        if (chunk5 != null && chunk5.equals("!=")) {
                                            address4 = new TreeNode(input.substring(offset, offset + 2), offset, new ArrayList<TreeNode>());
                                            offset = offset + 2;
                                        } else {
                                            address4 = FAILURE;
                                            if (offset > failure) {
                                                failure = offset;
                                                expected = new ArrayList<String[]>();
                                            }
                                            if (offset == failure) {
                                                expected.add(new String[] { "LXFExpression::comparison", "\"!=\"" });
                                            }
                                        }
                                        if (address4 == FAILURE) {
                                            offset = index4;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_additive();
                        if (address5 != FAILURE) {
                            elements2.add(1, address5);
                        } else {
                            elements2 = null;
                            offset = index3;
                        }
                    } else {
                        elements2 = null;
                        offset = index3;
                    }
                    if (elements2 == null) {
                        address3 = FAILURE;
                    } else {
                        address3 = new TreeNode8(input.substring(index3, offset), index3, elements2);
                        offset = offset;
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode7(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_additive() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.additive);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.additive, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_multiplicative();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    int index3 = offset;
                    List<TreeNode> elements2 = new ArrayList<TreeNode>(2);
                    TreeNode address4 = FAILURE;
                    int index4 = offset;
                    String chunk0 = null;
                    int max0 = offset + 1;
                    if (max0 <= inputSize) {
                        chunk0 = input.substring(offset, max0);
                    }
                    if (chunk0 != null && chunk0.equals("+")) {
                        address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::additive", "\"+\"" });
                        }
                    }
                    if (address4 == FAILURE) {
                        offset = index4;
                        String chunk1 = null;
                        int max1 = offset + 1;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && chunk1.equals("-")) {
                            address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::additive", "\"-\"" });
                            }
                        }
                        if (address4 == FAILURE) {
                            offset = index4;
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_multiplicative();
                        if (address5 != FAILURE) {
                            elements2.add(1, address5);
                        } else {
                            elements2 = null;
                            offset = index3;
                        }
                    } else {
                        elements2 = null;
                        offset = index3;
                    }
                    if (elements2 == null) {
                        address3 = FAILURE;
                    } else {
                        address3 = new TreeNode10(input.substring(index3, offset), index3, elements2);
                        offset = offset;
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode9(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_multiplicative() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.multiplicative);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.multiplicative, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_power();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    int index3 = offset;
                    List<TreeNode> elements2 = new ArrayList<TreeNode>(2);
                    TreeNode address4 = FAILURE;
                    int index4 = offset;
                    String chunk0 = null;
                    int max0 = offset + 1;
                    if (max0 <= inputSize) {
                        chunk0 = input.substring(offset, max0);
                    }
                    if (chunk0 != null && chunk0.equals("*")) {
                        address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::multiplicative", "\"*\"" });
                        }
                    }
                    if (address4 == FAILURE) {
                        offset = index4;
                        String chunk1 = null;
                        int max1 = offset + 1;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && chunk1.equals("/")) {
                            address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::multiplicative", "\"/\"" });
                            }
                        }
                        if (address4 == FAILURE) {
                            offset = index4;
                            String chunk2 = null;
                            int max2 = offset + 1;
                            if (max2 <= inputSize) {
                                chunk2 = input.substring(offset, max2);
                            }
                            if (chunk2 != null && chunk2.equals("%")) {
                                address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                                offset = offset + 1;
                            } else {
                                address4 = FAILURE;
                                if (offset > failure) {
                                    failure = offset;
                                    expected = new ArrayList<String[]>();
                                }
                                if (offset == failure) {
                                    expected.add(new String[] { "LXFExpression::multiplicative", "\"%\"" });
                                }
                            }
                            if (address4 == FAILURE) {
                                offset = index4;
                            }
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_power();
                        if (address5 != FAILURE) {
                            elements2.add(1, address5);
                        } else {
                            elements2 = null;
                            offset = index3;
                        }
                    } else {
                        elements2 = null;
                        offset = index3;
                    }
                    if (elements2 == null) {
                        address3 = FAILURE;
                    } else {
                        address3 = new TreeNode12(input.substring(index3, offset), index3, elements2);
                        offset = offset;
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode11(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_power() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.power);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.power, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_unary();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    int index3 = offset;
                    List<TreeNode> elements2 = new ArrayList<TreeNode>(2);
                    TreeNode address4 = FAILURE;
                    String chunk0 = null;
                    int max0 = offset + 1;
                    if (max0 <= inputSize) {
                        chunk0 = input.substring(offset, max0);
                    }
                    if (chunk0 != null && chunk0.equals("^")) {
                        address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::power", "\"^\"" });
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_unary();
                        if (address5 != FAILURE) {
                            elements2.add(1, address5);
                        } else {
                            elements2 = null;
                            offset = index3;
                        }
                    } else {
                        elements2 = null;
                        offset = index3;
                    }
                    if (elements2 == null) {
                        address3 = FAILURE;
                    } else {
                        address3 = new TreeNode14(input.substring(index3, offset), index3, elements2);
                        offset = offset;
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode13(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_unary() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.unary);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.unary, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            int index2 = offset;
            List<TreeNode> elements1 = new ArrayList<TreeNode>();
            TreeNode address2 = null;
            while (true) {
                int index3 = offset;
                String chunk0 = null;
                int max0 = offset + 1;
                if (max0 <= inputSize) {
                    chunk0 = input.substring(offset, max0);
                }
                if (chunk0 != null && chunk0.equals("-")) {
                    address2 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address2 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::unary", "\"-\"" });
                    }
                }
                if (address2 == FAILURE) {
                    offset = index3;
                    String chunk1 = null;
                    int max1 = offset + 1;
                    if (max1 <= inputSize) {
                        chunk1 = input.substring(offset, max1);
                    }
                    if (chunk1 != null && chunk1.equals("!")) {
                        address2 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address2 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::unary", "\"!\"" });
                        }
                    }
                    if (address2 == FAILURE) {
                        offset = index3;
                    }
                }
                if (address2 != FAILURE) {
                    elements1.add(address2);
                } else {
                    break;
                }
            }
            if (elements1.size() >= 0) {
                address1 = new TreeNode(input.substring(index2, offset), index2, elements1);
                offset = offset;
            } else {
                address1 = FAILURE;
            }
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address3 = FAILURE;
                address3 = _read_primary();
                if (address3 != FAILURE) {
                    elements0.add(1, address3);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode15(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_primary() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.primary);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.primary, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            address0 = _read_number();
            if (address0 == FAILURE) {
                offset = index1;
                address0 = _read_boolean();
                if (address0 == FAILURE) {
                    offset = index1;
                    address0 = _read_variable();
                    if (address0 == FAILURE) {
                        offset = index1;
                        address0 = _read_function();
                        if (address0 == FAILURE) {
                            offset = index1;
                            int index2 = offset;
                            List<TreeNode> elements0 = new ArrayList<TreeNode>(3);
                            TreeNode address1 = FAILURE;
                            String chunk0 = null;
                            int max0 = offset + 1;
                            if (max0 <= inputSize) {
                                chunk0 = input.substring(offset, max0);
                            }
                            if (chunk0 != null && chunk0.equals("(")) {
                                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                                offset = offset + 1;
                            } else {
                                address1 = FAILURE;
                                if (offset > failure) {
                                    failure = offset;
                                    expected = new ArrayList<String[]>();
                                }
                                if (offset == failure) {
                                    expected.add(new String[] { "LXFExpression::primary", "\"(\"" });
                                }
                            }
                            if (address1 != FAILURE) {
                                elements0.add(0, address1);
                                TreeNode address2 = FAILURE;
                                address2 = _read_expression();
                                if (address2 != FAILURE) {
                                    elements0.add(1, address2);
                                    TreeNode address3 = FAILURE;
                                    String chunk1 = null;
                                    int max1 = offset + 1;
                                    if (max1 <= inputSize) {
                                        chunk1 = input.substring(offset, max1);
                                    }
                                    if (chunk1 != null && chunk1.equals(")")) {
                                        address3 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                                        offset = offset + 1;
                                    } else {
                                        address3 = FAILURE;
                                        if (offset > failure) {
                                            failure = offset;
                                            expected = new ArrayList<String[]>();
                                        }
                                        if (offset == failure) {
                                            expected.add(new String[] { "LXFExpression::primary", "\")\"" });
                                        }
                                    }
                                    if (address3 != FAILURE) {
                                        elements0.add(2, address3);
                                    } else {
                                        elements0 = null;
                                        offset = index2;
                                    }
                                } else {
                                    elements0 = null;
                                    offset = index2;
                                }
                            } else {
                                elements0 = null;
                                offset = index2;
                            }
                            if (elements0 == null) {
                                address0 = FAILURE;
                            } else {
                                address0 = new TreeNode16(input.substring(index2, offset), index2, elements0);
                                offset = offset;
                            }
                            if (address0 == FAILURE) {
                                offset = index1;
                            }
                        }
                    }
                }
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_function() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.function);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.function, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_function_name();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                address2 = _read_unary();
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode17(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_function_name() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.function_name);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.function_name, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            String chunk0 = null;
            int max0 = offset + 3;
            if (max0 <= inputSize) {
                chunk0 = input.substring(offset, max0);
            }
            if (chunk0 != null && chunk0.equals("sin")) {
                address0 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                offset = offset + 3;
            } else {
                address0 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXFExpression::function_name", "\"sin\"" });
                }
            }
            if (address0 == FAILURE) {
                offset = index1;
                String chunk1 = null;
                int max1 = offset + 3;
                if (max1 <= inputSize) {
                    chunk1 = input.substring(offset, max1);
                }
                if (chunk1 != null && chunk1.equals("cos")) {
                    address0 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                    offset = offset + 3;
                } else {
                    address0 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::function_name", "\"cos\"" });
                    }
                }
                if (address0 == FAILURE) {
                    offset = index1;
                    String chunk2 = null;
                    int max2 = offset + 3;
                    if (max2 <= inputSize) {
                        chunk2 = input.substring(offset, max2);
                    }
                    if (chunk2 != null && chunk2.equals("tan")) {
                        address0 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                        offset = offset + 3;
                    } else {
                        address0 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::function_name", "\"tan\"" });
                        }
                    }
                    if (address0 == FAILURE) {
                        offset = index1;
                        String chunk3 = null;
                        int max3 = offset + 4;
                        if (max3 <= inputSize) {
                            chunk3 = input.substring(offset, max3);
                        }
                        if (chunk3 != null && chunk3.equals("asin")) {
                            address0 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                            offset = offset + 4;
                        } else {
                            address0 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::function_name", "\"asin\"" });
                            }
                        }
                        if (address0 == FAILURE) {
                            offset = index1;
                            String chunk4 = null;
                            int max4 = offset + 4;
                            if (max4 <= inputSize) {
                                chunk4 = input.substring(offset, max4);
                            }
                            if (chunk4 != null && chunk4.equals("acos")) {
                                address0 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                                offset = offset + 4;
                            } else {
                                address0 = FAILURE;
                                if (offset > failure) {
                                    failure = offset;
                                    expected = new ArrayList<String[]>();
                                }
                                if (offset == failure) {
                                    expected.add(new String[] { "LXFExpression::function_name", "\"acos\"" });
                                }
                            }
                            if (address0 == FAILURE) {
                                offset = index1;
                                String chunk5 = null;
                                int max5 = offset + 4;
                                if (max5 <= inputSize) {
                                    chunk5 = input.substring(offset, max5);
                                }
                                if (chunk5 != null && chunk5.equals("atan")) {
                                    address0 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                                    offset = offset + 4;
                                } else {
                                    address0 = FAILURE;
                                    if (offset > failure) {
                                        failure = offset;
                                        expected = new ArrayList<String[]>();
                                    }
                                    if (offset == failure) {
                                        expected.add(new String[] { "LXFExpression::function_name", "\"atan\"" });
                                    }
                                }
                                if (address0 == FAILURE) {
                                    offset = index1;
                                    String chunk6 = null;
                                    int max6 = offset + 3;
                                    if (max6 <= inputSize) {
                                        chunk6 = input.substring(offset, max6);
                                    }
                                    if (chunk6 != null && chunk6.equals("deg")) {
                                        address0 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                                        offset = offset + 3;
                                    } else {
                                        address0 = FAILURE;
                                        if (offset > failure) {
                                            failure = offset;
                                            expected = new ArrayList<String[]>();
                                        }
                                        if (offset == failure) {
                                            expected.add(new String[] { "LXFExpression::function_name", "\"deg\"" });
                                        }
                                    }
                                    if (address0 == FAILURE) {
                                        offset = index1;
                                        String chunk7 = null;
                                        int max7 = offset + 3;
                                        if (max7 <= inputSize) {
                                            chunk7 = input.substring(offset, max7);
                                        }
                                        if (chunk7 != null && chunk7.equals("rad")) {
                                            address0 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                                            offset = offset + 3;
                                        } else {
                                            address0 = FAILURE;
                                            if (offset > failure) {
                                                failure = offset;
                                                expected = new ArrayList<String[]>();
                                            }
                                            if (offset == failure) {
                                                expected.add(new String[] { "LXFExpression::function_name", "\"rad\"" });
                                            }
                                        }
                                        if (address0 == FAILURE) {
                                            offset = index1;
                                            String chunk8 = null;
                                            int max8 = offset + 3;
                                            if (max8 <= inputSize) {
                                                chunk8 = input.substring(offset, max8);
                                            }
                                            if (chunk8 != null && chunk8.equals("abs")) {
                                                address0 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                                                offset = offset + 3;
                                            } else {
                                                address0 = FAILURE;
                                                if (offset > failure) {
                                                    failure = offset;
                                                    expected = new ArrayList<String[]>();
                                                }
                                                if (offset == failure) {
                                                    expected.add(new String[] { "LXFExpression::function_name", "\"abs\"" });
                                                }
                                            }
                                            if (address0 == FAILURE) {
                                                offset = index1;
                                                String chunk9 = null;
                                                int max9 = offset + 4;
                                                if (max9 <= inputSize) {
                                                    chunk9 = input.substring(offset, max9);
                                                }
                                                if (chunk9 != null && chunk9.equals("sqrt")) {
                                                    address0 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                                                    offset = offset + 4;
                                                } else {
                                                    address0 = FAILURE;
                                                    if (offset > failure) {
                                                        failure = offset;
                                                        expected = new ArrayList<String[]>();
                                                    }
                                                    if (offset == failure) {
                                                        expected.add(new String[] { "LXFExpression::function_name", "\"sqrt\"" });
                                                    }
                                                }
                                                if (address0 == FAILURE) {
                                                    offset = index1;
                                                    String chunk10 = null;
                                                    int max10 = offset + 5;
                                                    if (max10 <= inputSize) {
                                                        chunk10 = input.substring(offset, max10);
                                                    }
                                                    if (chunk10 != null && chunk10.equals("floor")) {
                                                        address0 = new TreeNode(input.substring(offset, offset + 5), offset, new ArrayList<TreeNode>());
                                                        offset = offset + 5;
                                                    } else {
                                                        address0 = FAILURE;
                                                        if (offset > failure) {
                                                            failure = offset;
                                                            expected = new ArrayList<String[]>();
                                                        }
                                                        if (offset == failure) {
                                                            expected.add(new String[] { "LXFExpression::function_name", "\"floor\"" });
                                                        }
                                                    }
                                                    if (address0 == FAILURE) {
                                                        offset = index1;
                                                        String chunk11 = null;
                                                        int max11 = offset + 4;
                                                        if (max11 <= inputSize) {
                                                            chunk11 = input.substring(offset, max11);
                                                        }
                                                        if (chunk11 != null && chunk11.equals("ceil")) {
                                                            address0 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                                                            offset = offset + 4;
                                                        } else {
                                                            address0 = FAILURE;
                                                            if (offset > failure) {
                                                                failure = offset;
                                                                expected = new ArrayList<String[]>();
                                                            }
                                                            if (offset == failure) {
                                                                expected.add(new String[] { "LXFExpression::function_name", "\"ceil\"" });
                                                            }
                                                        }
                                                        if (address0 == FAILURE) {
                                                            offset = index1;
                                                            String chunk12 = null;
                                                            int max12 = offset + 5;
                                                            if (max12 <= inputSize) {
                                                                chunk12 = input.substring(offset, max12);
                                                            }
                                                            if (chunk12 != null && chunk12.equals("round")) {
                                                                address0 = new TreeNode(input.substring(offset, offset + 5), offset, new ArrayList<TreeNode>());
                                                                offset = offset + 5;
                                                            } else {
                                                                address0 = FAILURE;
                                                                if (offset > failure) {
                                                                    failure = offset;
                                                                    expected = new ArrayList<String[]>();
                                                                }
                                                                if (offset == failure) {
                                                                    expected.add(new String[] { "LXFExpression::function_name", "\"round\"" });
                                                                }
                                                            }
                                                            if (address0 == FAILURE) {
                                                                offset = index1;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_variable() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.variable);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.variable, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            int index2 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(4);
            TreeNode address1 = FAILURE;
            String chunk0 = null;
            int max0 = offset + 1;
            if (max0 <= inputSize) {
                chunk0 = input.substring(offset, max0);
            }
            if (chunk0 != null && chunk0.equals("$")) {
                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                offset = offset + 1;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXFExpression::variable", "\"$\"" });
                }
            }
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                String chunk1 = null;
                int max1 = offset + 1;
                if (max1 <= inputSize) {
                    chunk1 = input.substring(offset, max1);
                }
                if (chunk1 != null && chunk1.equals("{")) {
                    address2 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address2 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::variable", "\"{\"" });
                    }
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                    TreeNode address3 = FAILURE;
                    address3 = _read_identifier();
                    if (address3 != FAILURE) {
                        elements0.add(2, address3);
                        TreeNode address4 = FAILURE;
                        String chunk2 = null;
                        int max2 = offset + 1;
                        if (max2 <= inputSize) {
                            chunk2 = input.substring(offset, max2);
                        }
                        if (chunk2 != null && chunk2.equals("}")) {
                            address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::variable", "\"}\"" });
                            }
                        }
                        if (address4 != FAILURE) {
                            elements0.add(3, address4);
                        } else {
                            elements0 = null;
                            offset = index2;
                        }
                    } else {
                        elements0 = null;
                        offset = index2;
                    }
                } else {
                    elements0 = null;
                    offset = index2;
                }
            } else {
                elements0 = null;
                offset = index2;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode18(input.substring(index2, offset), index2, elements0);
                offset = offset;
            }
            if (address0 == FAILURE) {
                offset = index1;
                int index3 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>(2);
                TreeNode address5 = FAILURE;
                String chunk3 = null;
                int max3 = offset + 1;
                if (max3 <= inputSize) {
                    chunk3 = input.substring(offset, max3);
                }
                if (chunk3 != null && chunk3.equals("$")) {
                    address5 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address5 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::variable", "\"$\"" });
                    }
                }
                if (address5 != FAILURE) {
                    elements1.add(0, address5);
                    TreeNode address6 = FAILURE;
                    address6 = _read_identifier();
                    if (address6 != FAILURE) {
                        elements1.add(1, address6);
                    } else {
                        elements1 = null;
                        offset = index3;
                    }
                } else {
                    elements1 = null;
                    offset = index3;
                }
                if (elements1 == null) {
                    address0 = FAILURE;
                } else {
                    address0 = new TreeNode19(input.substring(index3, offset), index3, elements1);
                    offset = offset;
                }
                if (address0 == FAILURE) {
                    offset = index1;
                }
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_identifier() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.identifier);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.identifier, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            String chunk0 = null;
            int max0 = offset + 1;
            if (max0 <= inputSize) {
                chunk0 = input.substring(offset, max0);
            }
            if (chunk0 != null && REGEX_1.matcher(chunk0).matches()) {
                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                offset = offset + 1;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXFExpression::identifier", "[a-zA-Z]" });
                }
            }
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    String chunk1 = null;
                    int max1 = offset + 1;
                    if (max1 <= inputSize) {
                        chunk1 = input.substring(offset, max1);
                    }
                    if (chunk1 != null && REGEX_2.matcher(chunk1).matches()) {
                        address3 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address3 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXFExpression::identifier", "[a-zA-Z0-9]" });
                        }
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index2, offset), index2, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_boolean() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.boolean);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.boolean, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            String chunk0 = null;
            int max0 = offset + 4;
            if (max0 <= inputSize) {
                chunk0 = input.substring(offset, max0);
            }
            if (chunk0 != null && chunk0.equals("true")) {
                address0 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                offset = offset + 4;
            } else {
                address0 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXFExpression::boolean", "\"true\"" });
                }
            }
            if (address0 == FAILURE) {
                offset = index1;
                String chunk1 = null;
                int max1 = offset + 5;
                if (max1 <= inputSize) {
                    chunk1 = input.substring(offset, max1);
                }
                if (chunk1 != null && chunk1.equals("false")) {
                    address0 = new TreeNode(input.substring(offset, offset + 5), offset, new ArrayList<TreeNode>());
                    offset = offset + 5;
                } else {
                    address0 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::boolean", "\"false\"" });
                    }
                }
                if (address0 == FAILURE) {
                    offset = index1;
                }
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_number() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.number);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.number, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            address0 = _read_float();
            if (address0 == FAILURE) {
                offset = index1;
                address0 = _read_integer();
                if (address0 == FAILURE) {
                    offset = index1;
                }
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_float() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.float);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.float, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(3);
            TreeNode address1 = FAILURE;
            address1 = _read_integer();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                String chunk0 = null;
                int max0 = offset + 1;
                if (max0 <= inputSize) {
                    chunk0 = input.substring(offset, max0);
                }
                if (chunk0 != null && chunk0.equals(".")) {
                    address2 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address2 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::float", "\".\"" });
                    }
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                    TreeNode address3 = FAILURE;
                    int index2 = offset;
                    List<TreeNode> elements1 = new ArrayList<TreeNode>();
                    TreeNode address4 = null;
                    while (true) {
                        String chunk1 = null;
                        int max1 = offset + 1;
                        if (max1 <= inputSize) {
                            chunk1 = input.substring(offset, max1);
                        }
                        if (chunk1 != null && REGEX_3.matcher(chunk1).matches()) {
                            address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                            offset = offset + 1;
                        } else {
                            address4 = FAILURE;
                            if (offset > failure) {
                                failure = offset;
                                expected = new ArrayList<String[]>();
                            }
                            if (offset == failure) {
                                expected.add(new String[] { "LXFExpression::float", "[0-9]" });
                            }
                        }
                        if (address4 != FAILURE) {
                            elements1.add(address4);
                        } else {
                            break;
                        }
                    }
                    if (elements1.size() >= 1) {
                        address3 = new TreeNode(input.substring(index2, offset), index2, elements1);
                        offset = offset;
                    } else {
                        address3 = FAILURE;
                    }
                    if (address3 != FAILURE) {
                        elements0.add(2, address3);
                    } else {
                        elements0 = null;
                        offset = index1;
                    }
                } else {
                    elements0 = null;
                    offset = index1;
                }
            } else {
                elements0 = null;
                offset = index1;
            }
            if (elements0 == null) {
                address0 = FAILURE;
            } else {
                address0 = new TreeNode20(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_integer() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.integer);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.integer, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>();
            TreeNode address1 = null;
            while (true) {
                String chunk0 = null;
                int max0 = offset + 1;
                if (max0 <= inputSize) {
                    chunk0 = input.substring(offset, max0);
                }
                if (chunk0 != null && REGEX_4.matcher(chunk0).matches()) {
                    address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address1 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::integer", "[0-9]" });
                    }
                }
                if (address1 != FAILURE) {
                    elements0.add(address1);
                } else {
                    break;
                }
            }
            if (elements0.size() >= 1) {
                address0 = new TreeNode(input.substring(index1, offset), index1, elements0);
                offset = offset;
            } else {
                address0 = FAILURE;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_whitespace() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.whitespace);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.whitespace, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>();
            TreeNode address1 = null;
            while (true) {
                String chunk0 = null;
                int max0 = offset + 1;
                if (max0 <= inputSize) {
                    chunk0 = input.substring(offset, max0);
                }
                if (chunk0 != null && REGEX_5.matcher(chunk0).matches()) {
                    address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address1 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXFExpression::whitespace", "[ \\t]" });
                    }
                }
                if (address1 != FAILURE) {
                    elements0.add(address1);
                } else {
                    break;
                }
            }
            if (elements0.size() >= 0) {
                address0 = new TreeNode(input.substring(index1, offset), index1, elements0);
                offset = offset;
            } else {
                address0 = FAILURE;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }
}
