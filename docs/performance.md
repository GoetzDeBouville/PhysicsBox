# Performance

PhysicsBox uses a fixed‑timestep loop. Performance primarily depends on `StepConfig`, world scale, and the number of bodies/contacts.

## StepConfig knobs
- `hz`: higher values improve responsiveness but increase CPU usage.
- `maxSubSteps`: limits catch‑up work; too high can cause long frames.
- `maxDeltaSeconds`: clamps large frame deltas to avoid unstable steps.
- `velocityIterations` / `positionIterations`: higher values improve stability but cost CPU.

## World scale
Very large `PxPerMeter` makes bodies extremely small in meters, which can increase solver work. Keep common body sizes in ~0.1–2.0 meters.

## Tips
!!! tip "Start with defaults"
    Defaults like `hz=60`, `velocityIterations=8`, `positionIterations=3` are good for most UI scenes.

!!! tip "Use sleeping"
    Keep `allowSleep=true` on most bodies to reduce CPU usage when objects rest.
