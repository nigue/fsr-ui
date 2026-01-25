package com.tapir.fsr.model;

import java.util.List;

public record Profile(String name,
                      List<Integer> thresholds,
                      boolean active) {
}
