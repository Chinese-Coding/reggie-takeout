package cn.bupt.edu.zfq.reggietakeout.common;

/**
 * 关联删除异常
 *
 * <p>
 * 删除数据时, 此数据关联了其他数据时抛出此异常
 */
public class AssociationDeletionException extends RuntimeException {
    public AssociationDeletionException(String message) {
        super(message);
    }
}
