# ClickBait App Constitution

## Core Principles

### I. Ship Fast, Iterate Often
Build the simplest version that works. Avoid over-engineering. New features start small and grow based on user feedback.

### II. Engagement-First Design
Every feature must be evaluated by its impact on user engagement and click-through rates. Data and metrics drive decisions.

### III. Test What Matters
Focus testing on core business logic (headline generation, scoring, A/B logic). UI tests are optional; unit + integration tests on critical paths are not.

### IV. Simplicity Over Cleverness
Prefer boring, readable code. No premature optimization. If a solution needs a long explanation, simplify it.

### V. Explicit Over Implicit
Configuration, data flow, and side effects should be obvious from reading the code. No magic.

## Technology Constraints
- Language/runtime decided per project; document choice in spec before implementation.
- No external dependencies without justification.
- All secrets via environment variables — never hardcoded.

## Governance
This constitution supersedes all other development guidelines. Any exception requires a documented rationale in the relevant spec.

**Version**: 1.0.0 | **Ratified**: 2026-02-23 | **Last Amended**: 2026-02-23
