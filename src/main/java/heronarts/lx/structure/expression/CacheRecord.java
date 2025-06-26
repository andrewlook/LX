/**
 * This file was generated from ../../../expression.peg
 * See https://canopy.jcoglan.com/ for documentation
 */

package heronarts.lx.structure.expression;

class CacheRecord {
    TreeNode node;
    int tail;

    CacheRecord(TreeNode node, int tail) {
        this.node = node;
        this.tail = tail;
    }
}
