/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.lemming.common.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import sun.misc.Unsafe;

/**
 * 并发环形列表
 * 不存储null值
 * 增加时:线程增加节点时,从head进行遍历,同时尝试对
 * 节点进行占用,占用的可进行添加.当无法进行占用的节点,
 * 向后遍历尝试,一直无法进行占用的,在tail节点进行排队尝试.
 * 删除时:遍历节点,对需要删除节点的前驱节点进行占用,然后对
 * 需删除的节点进行占用,占用后进行删除,失败着循环尝试.
 * 
 * @author hongyu
 * @param <T>
 * @date 2019-08
 * @description
 */
@SuppressWarnings("restriction")
public class CycleList<T> implements Collection<T> {

    /**
     * 链表头head不存储任何值
     */
    private Node head;
    /**
     * 链表尾部,tail.next = head,初始时head.next = tail
     */
    private volatile Node tail;

    private static final long tailOffset;
    private static Unsafe unsafe = null;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            tailOffset = unsafe.objectFieldOffset(CycleList.class.getDeclaredField("tail"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    /**
     * 链表的节点
     * 
     * @author hongyu
     * @date 2019-08
     * @description
     *              occupy代表节点被占用,其他线程无法操作
     */
    private class Node {
        /**
         * 节点值,除了head值null外,不接受null值
         */
        private Object value;
        private Node next;
        /**
         * 0标识未占用,1标识已占用.已占用的节点,无法对紧接其后面的链进行任何操作
         */
        private AtomicInteger occupy;

        public Node(Object v) {
            this.value = v;
            next = null;
            this.occupy = new AtomicInteger(0);
        }

        public final boolean tryOccupy() {
            return !isOccupy() && occupy.compareAndSet(0, 1);
        }

        public final boolean tryReleaseOccupy() {
            return isOccupy() && this.occupy.compareAndSet(1, 0);
        }

        private final boolean isOccupy() {
            return occupy.get() == 1;
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node h = head.next;
        while (h != head) {
            if (h.next == head) {
                sb.append(h.value.toString());
            } else {
                sb.append(h.value.toString() + ",");
            }
            h = h.next;
        }
        return sb.toString();
    }

    /**
     * 初始化一个环形链表
     */
    public CycleList() {
        head = new Node(null);
        tail = head;
        head.next = tail;
        tail.next = head;
    }

    //
    /**
     * 尝试在head进行添加
     * 
     * @param v
     * @return
     */
    private boolean tryAddNode(Object v) {
        Node node = new Node(v);
        Node h = head;
        if (h != tail && h.tryOccupy()) {
            try {
                // 这里不会是首次添加节点,所有tail处添加节点,都交给最后的循环尝试
                if (h != tail) {
                    node.next = h.next;
                    h.next = node;
                    return true;
                }
            } finally {
                h.tryReleaseOccupy();
            }
        }
        return this.doTryAddNode(h.next, node);
    }

    /**
     * 遍历整个链表进行添加
     * 
     * @param start
     * @param node
     * @return
     */
    private boolean doTryAddNode(Node start, Node node) {
        Node t = tail;
        // 这里的tail其实会变,但是无所谓,我们只在当前的链表长度内进行添加,其他走后面
        for (Node h = start; h != t; h = h.next) {
            // 并发删除的时候可能导致为null
            if (h == null) {
                break;
            }
            if (h.tryOccupy()) {
                try {
                    // 在h被占用前可能被删除了,所以h.next就等于空了
                    // 相当于其实h已经和主链表已经脱离了
                    if (h.next == null || h == tail) {
                        break;
                    }
                    node.next = h.next;
                    h.next = node;
                    return true;
                } finally {
                    h.tryReleaseOccupy();
                }
            }
        }
        return doAddAtTail(node);
    }

    /**
     * 所有在经过俩次尝试添加均失败的节点,都在tail处进行无限循环尝试
     * 
     * @param node
     * @return
     */
    private boolean doAddAtTail(Node node) {
        Node t;
        for (;;) {
            t = tail;
            if (t.tryOccupy()) {
                try {
                    if (t != tail) {
                        continue;
                    }
                    // 这里肯定成功
                    node.tryOccupy();
                    try {
                        if (setTail(t, node)) {
                            // 线程1执行完到这里node已经是tail了,然后又线程2占用了tail(此时即node),
                            // 形成了node->node1->head.然后线程1再执行
                            // 形成了t->node->head,导致node1其实相当于没添加成功.
                            // 因此必须在if外面先把node给默认占用了,这样线程2其实是无法执行的
                            node.next = head;
                            t.next = node;
                        } else {
                            continue;
                        }
                    } finally {
                        node.tryReleaseOccupy();
                    }
                    return true;
                } finally {
                    t.tryReleaseOccupy();
                }
            }
        }
    }

    /**
     * 在tail添加新节点的时候使用
     * 
     * @param ttail
     *            原先的tail节点
     * @param update
     *            即将作为tail的节点
     * @return
     */
    private boolean setTail(Node ttail, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, ttail, update);
    }

    /**
     * size is not precise
     */
    @Override
    public int size() {
        final Node th = head;
        if (th == tail) {
            return 0;
        }
        Node h = th;
        int count = 0;
        while (h.next != th) {
            h = h.next;
            if (h == null) {
                break;
            }
            count++;
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        Node h = head;
        return h.next == h;
    }

    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {
            private Node h = head;

            @Override
            public boolean hasNext() {
                return h.next != null && h.next != head;
            }

            @Override
            public T next() {
                Node next = h.next;
                if (next != null) {
                    h = h.next;
                    @SuppressWarnings("unchecked")
                    T o = (T) next.value;
                    return o;
                }
                return null;
            }

            @Override
            public void remove() {
                Node n = h.next;
                removeNode(h);
                h = n;
            }

        };
    }

    @Override
    public Object[] toArray() {
        int size = this.size();
        Object[] arr = new Object[size];
        Node h = head.next;
        int count = 0;
        // 并发可能为空指针
        while (h != head && count < size) {
            arr[count++] = h.value;
            h = h.next;
            if (h == null) {
                break;
            }
        }
        return arr;
    }

    @SuppressWarnings({ "hiding", "unchecked" })
    @Override
    public <T> T[] toArray(T[] a) {
        int size = this.size();
        a = (T[]) java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), size);
        if (size <= 0) {
            return a;
        }
        Object[] arr = a;
        Node h = head.next;
        int count = 0;
        // 并发可能为空指针
        while (h != head && count < size) {
            arr[count++] = h.value;
            h = h.next;
            if (h == null) {
                break;
            }
        }
        return a;
    }

    @Override
    public boolean add(T e) {
        if (e == null) {
            return false;
        }
        return this.tryAddNode(e);
    }

    /**
     * 对于相同元素的添加,删除只会删除其中的第一个
     */
    @Override
    public boolean remove(Object v) {
        if (v == null) {
            return false;
        }
        Node pre = head;
        Node n = pre.next;
        // 对可能head.next就是要删除的数进行尝试删除
        if (n.value == v) {
            if (pre.tryOccupy()) {
                try {
                    if (n.tryOccupy()) {
                        try {
                            if (n == pre.next && v.equals(n.value)) {
                                // tail的删除交给后面
                                if (n != tail) {
                                    pre.next = n.next;
                                    cleanNode(n);
                                    return true;
                                }
                            }
                        } finally {
                            n.tryReleaseOccupy();
                        }
                    }
                } finally {
                    pre.tryReleaseOccupy();
                }
            }
        }
        return this.doRemove(head, head.next, v);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Iterator<?> iter = c.iterator();
        while (iter.hasNext()) {
            remove(iter.next());
        }
        return true;
    }

    private boolean doRemove(Node pre, Node node, Object v) {
        // 这里可能pre.next已经不等于node了
        Node p = pre;
        for (Node h = node; h != head; p = h, h = h.next) {
            final Node m = h;
            if (m == null) {
                h = pre;
                continue;
            }
            if (v.equals(m.value)) {
                if (p.tryOccupy()) {
                    try {
                        // 并发下, 可能导致p.next已经变了,但是h必然是在p的后面,而不会跑去前面
                        if (p.next != m) {
                            // 这里说明已经遍历结束了
                            if (p.next == head) {
                                return false;
                            }
                            h = p;
                            break;
                        }
                        // 循环尝试占用
                        for (;;) {
                            if (m.tryOccupy()) {
                                try {
                                    // double check;并发下, 可能导致p.next已经变了,但是h必然是在p的后面,而不会跑去前面
                                    if (p.next != m) {
                                        if (p.next == head) {
                                            return false;
                                        }
                                        // 这里不使用m是因为m是需要释放的,否则会导致释放的节点不一致
                                        h = p;
                                        break;
                                    }
                                    if (m == tail) {
                                        if (setTail(m, p)) {
                                            // 这里m.next == head
                                            p.next = head;
                                            cleanNode(m);
                                            return true;
                                        } else {
                                            h = p;
                                            break;
                                        }
                                    } else {
                                        p.next = m.next;
                                        cleanNode(m);
                                        return true;
                                    }
                                } finally {
                                    m.tryReleaseOccupy();
                                }
                            }
                        }
                    } finally {
                        p.tryReleaseOccupy();
                    }
                } else {
                    h = p;
                }
            }
        }
        return false;
    }

    private final void cleanNode(Node node) {
        // for gc
        node.value = null;
        // 可能导致其他遍历的地方node为空
        node.next = null;
    }

    private boolean removeNode(Node node) {
        return remove(node.value);
    }

    /**
     * 并发不精确
     * contain是指当前队列中有的,可能当前返回true,但是并发同时又被删除了
     */
    @Override
    public boolean contains(Object o) {
        if (o == null || head.next == null) {
            return false;
        }
        final Node th = head;
        Node h = head.next;
        while (h != th) {
            if (o.equals(h.value)) {
                return true;
            }
            h = h.next;
            if (h == null) {
                break;
            }
        }
        return false;
    }

    /**
     * 并发不精确
     * contain是指当前队列中有的,可能当前返回true,但是并发同时又被删除了
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        Iterator<?> iter = c.iterator();
        while (iter.hasNext()) {
            if (!this.contains(iter.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据t.toString()获取更完整的在list中的对象
     * 
     * @param t
     * @return
     */
    @SuppressWarnings("unchecked")
    public T get(T query) {
        if (query == null || head.next == null) {
            return null;
        }
        final Node th = head;
        Node h = head.next;
        while (h != th) {
            if (query.equals(h.value)) {
                return (T) h.value;
            }
            h = h.next;
            if (h == null) {
                break;
            }
        }
        return null;

    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        Iterator<?> iter = c.iterator();
        while (iter.hasNext()) {
            tryAddNode(iter.next());
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO
        return false;
    }

    @Override
    public void clear() {
        Node h = head;
        for (;;) {
            if (h.tryOccupy()) {
                try {
                    h.next = head;
                    if (!setTail(tail, h)) {
                        continue;
                    }
                    return;
                } finally {
                    h.tryReleaseOccupy();
                }
            }
        }
    }

}
