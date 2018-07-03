package pt.um.lei.masb.blockchain.data;

import java.math.BigDecimal;

public interface SelfInterval<T> {
    BigDecimal calculateDiff(T before);
}
