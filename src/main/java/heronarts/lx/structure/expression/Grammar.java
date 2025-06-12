/**
 * This file was generated from expression.peg
 * See https://canopy.jcoglan.com/ for documentation
 */

package expression;

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

    private static Pattern REGEX_1 = Pattern.compile("\\A[a-z0-9-]");
    private static Pattern REGEX_2 = Pattern.compile("\\A[0-9]");
    private static Pattern REGEX_3 = Pattern.compile("\\A[^ ?]");
    private static Pattern REGEX_4 = Pattern.compile("\\A[^ #]");
    private static Pattern REGEX_5 = Pattern.compile("\\A[^ ]");

    TreeNode _read_url() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.url);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.url, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(6);
            TreeNode address1 = FAILURE;
            address1 = _read_scheme();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                String chunk0 = null;
                int max0 = offset + 3;
                if (max0 <= inputSize) {
                    chunk0 = input.substring(offset, max0);
                }
                if (chunk0 != null && chunk0.equals("://")) {
                    address2 = new TreeNode(input.substring(offset, offset + 3), offset, new ArrayList<TreeNode>());
                    offset = offset + 3;
                } else {
                    address2 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXExpression::url", "\"://\"" });
                    }
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
                    TreeNode address3 = FAILURE;
                    address3 = _read_host();
                    if (address3 != FAILURE) {
                        elements0.add(2, address3);
                        TreeNode address4 = FAILURE;
                        address4 = _read_pathname();
                        if (address4 != FAILURE) {
                            elements0.add(3, address4);
                            TreeNode address5 = FAILURE;
                            address5 = _read_search();
                            if (address5 != FAILURE) {
                                elements0.add(4, address5);
                                TreeNode address6 = FAILURE;
                                int index2 = offset;
                                address6 = _read_hash();
                                if (address6 == FAILURE) {
                                    address6 = new TreeNode(input.substring(index2, index2), index2, new ArrayList<TreeNode>());
                                    offset = index2;
                                }
                                if (address6 != FAILURE) {
                                    elements0.add(5, address6);
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
                address0 = new TreeNode1(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_scheme() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.scheme);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.scheme, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            String chunk0 = null;
            int max0 = offset + 4;
            if (max0 <= inputSize) {
                chunk0 = input.substring(offset, max0);
            }
            if (chunk0 != null && chunk0.equals("http")) {
                address1 = new TreeNode(input.substring(offset, offset + 4), offset, new ArrayList<TreeNode>());
                offset = offset + 4;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXExpression::scheme", "\"http\"" });
                }
            }
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                String chunk1 = null;
                int max1 = offset + 1;
                if (max1 <= inputSize) {
                    chunk1 = input.substring(offset, max1);
                }
                if (chunk1 != null && chunk1.equals("s")) {
                    address2 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                    offset = offset + 1;
                } else {
                    address2 = FAILURE;
                    if (offset > failure) {
                        failure = offset;
                        expected = new ArrayList<String[]>();
                    }
                    if (offset == failure) {
                        expected.add(new String[] { "LXExpression::scheme", "\"s\"" });
                    }
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
                address0 = new TreeNode(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_host() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.host);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.host, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_hostname();
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index2 = offset;
                address2 = _read_port();
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
                address0 = new TreeNode2(input.substring(index1, offset), index1, elements0);
                offset = offset;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_hostname() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.hostname);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.hostname, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            address1 = _read_segment();
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
                    if (chunk0 != null && chunk0.equals(".")) {
                        address4 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address4 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXExpression::hostname", "\".\"" });
                        }
                    }
                    if (address4 != FAILURE) {
                        elements2.add(0, address4);
                        TreeNode address5 = FAILURE;
                        address5 = _read_segment();
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

    TreeNode _read_segment() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.segment);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.segment, rule);
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
                        expected.add(new String[] { "LXExpression::segment", "[a-z0-9-]" });
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

    TreeNode _read_port() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.port);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.port, rule);
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
            if (chunk0 != null && chunk0.equals(":")) {
                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                offset = offset + 1;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXExpression::port", "\":\"" });
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
                            expected.add(new String[] { "LXExpression::port", "[0-9]" });
                        }
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 1) {
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

    TreeNode _read_pathname() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.pathname);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.pathname, rule);
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
            if (chunk0 != null && chunk0.equals("/")) {
                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                offset = offset + 1;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXExpression::pathname", "\"/\"" });
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
                    if (chunk1 != null && REGEX_3.matcher(chunk1).matches()) {
                        address3 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address3 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXExpression::pathname", "[^ ?]" });
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

    TreeNode _read_search() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.search);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.search, rule);
        }
        if (rule.containsKey(offset)) {
            address0 = rule.get(offset).node;
            offset = rule.get(offset).tail;
        } else {
            int index1 = offset;
            int index2 = offset;
            List<TreeNode> elements0 = new ArrayList<TreeNode>(2);
            TreeNode address1 = FAILURE;
            String chunk0 = null;
            int max0 = offset + 1;
            if (max0 <= inputSize) {
                chunk0 = input.substring(offset, max0);
            }
            if (chunk0 != null && chunk0.equals("?")) {
                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                offset = offset + 1;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXExpression::search", "\"?\"" });
                }
            }
            if (address1 != FAILURE) {
                elements0.add(0, address1);
                TreeNode address2 = FAILURE;
                int index3 = offset;
                List<TreeNode> elements1 = new ArrayList<TreeNode>();
                TreeNode address3 = null;
                while (true) {
                    String chunk1 = null;
                    int max1 = offset + 1;
                    if (max1 <= inputSize) {
                        chunk1 = input.substring(offset, max1);
                    }
                    if (chunk1 != null && REGEX_4.matcher(chunk1).matches()) {
                        address3 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address3 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXExpression::search", "[^ #]" });
                        }
                    }
                    if (address3 != FAILURE) {
                        elements1.add(address3);
                    } else {
                        break;
                    }
                }
                if (elements1.size() >= 0) {
                    address2 = new TreeNode(input.substring(index3, offset), index3, elements1);
                    offset = offset;
                } else {
                    address2 = FAILURE;
                }
                if (address2 != FAILURE) {
                    elements0.add(1, address2);
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
                address0 = new TreeNode5(input.substring(index2, offset), index2, elements0);
                offset = offset;
            }
            if (address0 == FAILURE) {
                address0 = new TreeNode(input.substring(index1, index1), index1, new ArrayList<TreeNode>());
                offset = index1;
            }
            rule.put(index0, new CacheRecord(address0, offset));
        }
        return address0;
    }

    TreeNode _read_hash() {
        TreeNode address0 = FAILURE;
        int index0 = offset;
        Map<Integer, CacheRecord> rule = cache.get(Label.hash);
        if (rule == null) {
            rule = new HashMap<Integer, CacheRecord>();
            cache.put(Label.hash, rule);
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
            if (chunk0 != null && chunk0.equals("#")) {
                address1 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                offset = offset + 1;
            } else {
                address1 = FAILURE;
                if (offset > failure) {
                    failure = offset;
                    expected = new ArrayList<String[]>();
                }
                if (offset == failure) {
                    expected.add(new String[] { "LXExpression::hash", "\"#\"" });
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
                    if (chunk1 != null && REGEX_5.matcher(chunk1).matches()) {
                        address3 = new TreeNode(input.substring(offset, offset + 1), offset, new ArrayList<TreeNode>());
                        offset = offset + 1;
                    } else {
                        address3 = FAILURE;
                        if (offset > failure) {
                            failure = offset;
                            expected = new ArrayList<String[]>();
                        }
                        if (offset == failure) {
                            expected.add(new String[] { "LXExpression::hash", "[^ ]" });
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
}
