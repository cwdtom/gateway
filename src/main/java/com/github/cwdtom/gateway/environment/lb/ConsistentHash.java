package com.github.cwdtom.gateway.environment.lb;

import com.github.cwdtom.gateway.constant.Constant;
import com.github.cwdtom.gateway.environment.MappingEnvironment;
import com.github.cwdtom.gateway.mapping.Mapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 一致性hash算法
 *
 * @author chenweidong
 * @since 2.1.0
 */
@Slf4j
public class ConsistentHash extends UrlMapping implements MappingEnvironment {
    /**
     * hash环
     */
    private Map<String, Node> hashLoop;

    public ConsistentHash(Map<String, List<Mapper>> urlMapping) {
        super(urlMapping);
        // 生成hash环
        for (Map.Entry<String, List<Mapper>> entry : urlMapping.entrySet()) {
            List<Mapper> mappers = entry.getValue();
            int sum = 0;
            for (Mapper mapper : mappers) {
                sum += mapper.getWeight();
            }
            List<Node> nodes = new LinkedList<>();
            for (Mapper mapper : mappers) {
                int count = Constant.MAX_NODE_SIZE * mapper.getWeight() / sum;
                StringBuilder sb = new StringBuilder(mapper.getTarget()).append("#");
                // 确保最少拥有一个节点
                if (count < 1) {
                    count = 1;
                }
                for (int i = 0; i < count; ++i) {
                    int hashCode = sb.append(i).toString().hashCode();
                    nodes.add(new Node(mapper, hashCode));
                }
            }
            // 排序
            Collections.sort(nodes);

            Map<String, Node> hashLoop = new HashMap<>(urlMapping.size() / 3 * 4);
            Node head = new Node();
            Node next = head;
            for (Node node : nodes) {
                next.next = node;
                next.next.prev = next;
                next = next.next;
            }
            head.next.prev = next;
            next.next = head.next;
            hashLoop.put(entry.getKey(), head);
            this.hashLoop = hashLoop;
        }
        log.info("ConsistentHash load completed.");
    }

    @Override
    public Mapper getLoadBalance(String host, String ip) {
        int hashCode = ip.hashCode();
        Node node = hashLoop.get(host);
        if (node == null) {
            return null;
        }
        if (hashCode > Constant.MID_INT) {
            node = node.prev;
            while (node.mapper != null) {
                if (node.hashCode > hashCode && node.mapper.isOnline()) {
                    break;
                }
                node = node.prev;
            }
        } else {
            node = node.next;
            while (node.mapper != null) {
                if (node.hashCode < hashCode && node.mapper.isOnline()) {
                    break;
                }
                node = node.next;
            }
        }
        return node.mapper;
    }

    /**
     * 节点
     */
    @NoArgsConstructor
    private class Node implements Comparable<Node> {
        /**
         * 映射对象
         */
        private Mapper mapper;
        /**
         * hash code
         */
        private Integer hashCode;
        /**
         * 后置节点
         */
        private Node next;
        /**
         * 前置节点
         */
        private Node prev;

        private Node(Mapper mapper, Integer hashCode) {
            this.mapper = mapper;
            this.hashCode = hashCode;
        }

        @Override
        public int compareTo(Node o) {
            return hashCode.compareTo(o.hashCode);
        }
    }
}
