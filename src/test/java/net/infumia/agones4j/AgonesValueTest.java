package net.infumia.agones4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

final class AgonesValueTest {

    @Test
    void counterExposesFields() {
        final AgonesCounter counter = new AgonesCounter("rooms", 10L, 3L);

        assertThat(counter.getName()).isEqualTo("rooms");
        assertThat(counter.getCapacity()).isEqualTo(10L);
        assertThat(counter.getCount()).isEqualTo(3L);
        assertThat(counter).hasToString("AgonesCounter[name='rooms', capacity=10, count=3]");
    }

    @Test
    void counterEqualityUsesAllFields() {
        final AgonesCounter counter = new AgonesCounter("rooms", 10L, 3L);

        assertThat(counter)
            .isEqualTo(counter)
            .isEqualTo(new AgonesCounter("rooms", 10L, 3L))
            .hasSameHashCodeAs(new AgonesCounter("rooms", 10L, 3L))
            .isNotEqualTo(new AgonesCounter("other", 10L, 3L))
            .isNotEqualTo(new AgonesCounter("rooms", 11L, 3L))
            .isNotEqualTo(new AgonesCounter("rooms", 10L, 4L))
            .isNotEqualTo(null)
            .isNotEqualTo("rooms");
    }

    @Test
    void listExposesFields() {
        final AgonesList list = new AgonesList("players", 4L, Arrays.asList("a", "b"));

        assertThat(list.getName()).isEqualTo("players");
        assertThat(list.getCapacity()).isEqualTo(4L);
        assertThat(list.getValues()).containsExactly("a", "b");
        assertThat(list).hasToString("AgonesList[name='players', capacity=4, values=[a, b]]");
    }

    @Test
    void listEqualityUsesAllFields() {
        final AgonesList list = new AgonesList("players", 4L, Arrays.asList("a", "b"));

        assertThat(list)
            .isEqualTo(list)
            .isEqualTo(new AgonesList("players", 4L, Arrays.asList("a", "b")))
            .hasSameHashCodeAs(new AgonesList("players", 4L, Arrays.asList("a", "b")))
            .isNotEqualTo(new AgonesList("other", 4L, Arrays.asList("a", "b")))
            .isNotEqualTo(new AgonesList("players", 5L, Arrays.asList("a", "b")))
            .isNotEqualTo(new AgonesList("players", 4L, Collections.singletonList("a")))
            .isNotEqualTo(null)
            .isNotEqualTo("players");
    }
}
