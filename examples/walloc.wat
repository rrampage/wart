(module
  (type (;0;) (func (param i32) (result i32)))
  (type (;1;) (func (param i32)))
  (type (;2;) (func))
  (import "env" "memory" (memory (;0;) 2))
  (func $walloc (type 0) (param i32) (result i32)
    local.get 0
    call $malloc)
  (func $wfree (type 1) (param i32)
    local.get 0
    call $free)
  (func $malloc (type 0) (param i32) (result i32)
    (local i32 i32 i32 i32 i32 i32)
    i32.const 0
    local.set 1
    block  ;; label = @1
      local.get 0
      i32.const 7
      i32.add
      local.tee 2
      i32.const 16
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 1
      local.set 1
      local.get 2
      i32.const 3
      i32.shr_u
      local.tee 3
      i32.const 2
      i32.eq
      br_if 0 (;@1;)
      i32.const 2
      local.set 1
      local.get 2
      i32.const 32
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 3
      local.set 1
      local.get 3
      i32.const 4
      i32.eq
      br_if 0 (;@1;)
      i32.const 4
      local.set 1
      local.get 2
      i32.const 48
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 5
      local.set 1
      local.get 3
      i32.const 6
      i32.eq
      br_if 0 (;@1;)
      i32.const 6
      local.set 1
      local.get 2
      i32.const 72
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 7
      local.set 1
      local.get 2
      i32.const 88
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 8
      local.set 1
      local.get 2
      i32.const 136
      i32.lt_u
      br_if 0 (;@1;)
      i32.const 9
      local.set 1
      local.get 2
      i32.const 264
      i32.lt_u
      br_if 0 (;@1;)
      local.get 0
      call $allocate_large_object
      local.tee 1
      i32.const 8
      i32.add
      i32.const 0
      local.get 1
      select
      return
    end
    block  ;; label = @1
      block  ;; label = @2
        local.get 1
        i32.const 2
        i32.shl
        i32.const 1088
        i32.add
        local.tee 4
        i32.load
        local.tee 2
        br_if 0 (;@2;)
        i32.const 0
        local.set 2
        block  ;; label = @3
          block  ;; label = @4
            i32.const 0
            i32.load offset=1124
            local.tee 0
            i32.eqz
            br_if 0 (;@4;)
            i32.const 0
            local.get 0
            i32.load
            i32.store offset=1124
            br 1 (;@3;)
          end
          i32.const 0
          call $allocate_large_object
          local.tee 0
          i32.eqz
          br_if 2 (;@1;)
        end
        local.get 0
        i32.const -65536
        i32.and
        local.tee 2
        local.get 0
        i32.const 8
        i32.shr_u
        i32.const 255
        i32.and
        local.tee 0
        i32.or
        local.get 1
        i32.store8
        local.get 0
        i32.const 8
        i32.shl
        local.get 2
        i32.or
        local.tee 0
        i32.const 256
        i32.add
        local.set 5
        local.get 0
        local.get 1
        i32.const 2
        i32.shl
        i32.const 1024
        i32.add
        i32.load
        local.tee 3
        i32.sub
        i32.const 256
        i32.add
        local.set 0
        i32.const 0
        local.set 2
        i32.const 0
        local.get 3
        i32.sub
        local.set 6
        local.get 3
        local.set 1
        block  ;; label = @3
          loop  ;; label = @4
            local.get 1
            i32.const 256
            i32.gt_u
            br_if 1 (;@3;)
            local.get 0
            local.get 2
            i32.store
            local.get 0
            local.get 6
            i32.add
            local.set 0
            local.get 5
            local.get 1
            i32.sub
            local.set 2
            local.get 1
            local.get 3
            i32.add
            local.set 1
            br 0 (;@4;)
          end
        end
        block  ;; label = @3
          local.get 2
          br_if 0 (;@3;)
          i32.const 0
          return
        end
        local.get 4
        local.get 2
        i32.store
      end
      local.get 4
      local.get 2
      i32.load
      i32.store
    end
    local.get 2)
  (func $allocate_large_object (type 0) (param i32) (result i32)
    (local i32 i32 i32 i32 i32 i32 i32 i32)
    i32.const 0
    local.set 1
    block  ;; label = @1
      i32.const 0
      i32.load8_u offset=1076
      i32.eqz
      br_if 0 (;@1;)
      i32.const 0
      i32.const 0
      i32.store8 offset=1076
      i32.const 1072
      local.set 2
      loop  ;; label = @2
        local.get 2
        i32.load
        local.tee 3
        i32.eqz
        br_if 1 (;@1;)
        local.get 3
        i32.const 8
        i32.add
        local.set 4
        local.get 3
        i32.load offset=4
        local.set 5
        block  ;; label = @3
          loop  ;; label = @4
            local.get 4
            local.get 5
            i32.add
            local.tee 6
            i32.const 8
            i32.shr_u
            i32.const 255
            i32.and
            local.tee 7
            i32.eqz
            br_if 1 (;@3;)
            local.get 6
            i32.const -65536
            i32.and
            local.get 7
            i32.or
            i32.load8_u
            i32.const 254
            i32.ne
            br_if 1 (;@3;)
            i32.const 1072
            local.set 7
            loop  ;; label = @5
              local.get 7
              local.tee 8
              i32.load
              local.tee 7
              local.get 6
              i32.ne
              br_if 0 (;@5;)
            end
            local.get 8
            local.get 6
            i32.load
            i32.store
            local.get 3
            local.get 5
            local.get 6
            i32.load offset=4
            i32.add
            i32.const 8
            i32.add
            local.tee 5
            i32.store offset=4
            local.get 8
            local.get 2
            local.get 2
            local.get 6
            i32.eq
            select
            local.set 2
            br 0 (;@4;)
          end
        end
        local.get 2
        i32.load
        local.set 2
        br 0 (;@2;)
      end
    end
    local.get 0
    i32.const 263
    i32.add
    i32.const -256
    i32.and
    local.set 3
    i32.const -1
    local.set 5
    i32.const 1072
    local.set 8
    i32.const 1072
    local.set 2
    block  ;; label = @1
      block  ;; label = @2
        block  ;; label = @3
          loop  ;; label = @4
            local.get 8
            i32.load
            local.tee 7
            i32.eqz
            br_if 1 (;@3;)
            block  ;; label = @5
              local.get 7
              i32.load offset=4
              local.tee 6
              local.get 0
              i32.lt_u
              br_if 0 (;@5;)
              local.get 6
              local.get 5
              i32.ge_u
              br_if 0 (;@5;)
              local.get 6
              local.set 5
              local.get 8
              local.set 2
              local.get 7
              local.set 1
              local.get 6
              i32.const 8
              i32.add
              local.get 3
              i32.ne
              br_if 0 (;@5;)
              local.get 8
              local.set 2
              local.get 6
              local.set 5
              local.get 7
              local.set 1
              br 3 (;@2;)
            end
            local.get 7
            local.set 8
            br 0 (;@4;)
          end
        end
        local.get 1
        br_if 0 (;@2;)
        memory.size
        i32.const 16
        i32.shl
        local.set 3
        local.get 0
        i32.const 264
        i32.add
        local.set 8
        i32.const 0
        local.set 4
        block  ;; label = @3
          block  ;; label = @4
            i32.const 0
            i32.load offset=1080
            local.tee 5
            i32.eqz
            br_if 0 (;@4;)
            i32.const 0
            local.set 6
            local.get 3
            local.set 7
            br 1 (;@3;)
          end
          i32.const 0
          local.get 3
          i32.const 132207
          i32.const -65536
          i32.and
          local.tee 7
          i32.sub
          local.tee 5
          i32.store offset=1080
          local.get 5
          local.set 6
        end
        block  ;; label = @3
          local.get 8
          local.get 6
          i32.le_u
          br_if 0 (;@3;)
          local.get 8
          local.get 6
          i32.sub
          local.tee 8
          local.get 5
          i32.const 1
          i32.shr_u
          local.tee 5
          local.get 5
          local.get 8
          i32.lt_u
          select
          i32.const 65535
          i32.add
          local.tee 8
          i32.const 16
          i32.shr_u
          memory.grow
          i32.const -1
          i32.eq
          br_if 2 (;@1;)
          i32.const 0
          i32.const 0
          i32.load offset=1080
          local.get 8
          i32.const -65536
          i32.and
          local.tee 4
          i32.add
          i32.store offset=1080
        end
        local.get 7
        i32.eqz
        br_if 1 (;@1;)
        local.get 7
        i32.const 255
        i32.store8 offset=1
        local.get 7
        i32.const 0
        i32.load offset=1072
        i32.store offset=256
        local.get 7
        i32.const 260
        i32.add
        local.get 4
        local.get 6
        i32.add
        i32.const -65536
        i32.and
        i32.const -264
        i32.add
        local.tee 5
        i32.store
        local.get 7
        i32.const 256
        i32.add
        local.set 1
      end
      local.get 1
      i32.const -65536
      i32.and
      local.tee 7
      local.get 1
      i32.const 8
      i32.shr_u
      i32.const 255
      i32.and
      i32.or
      i32.const 255
      i32.store8
      local.get 2
      local.get 1
      i32.load
      i32.store
      block  ;; label = @2
        local.get 5
        local.get 0
        i32.sub
        i32.const -256
        i32.and
        local.tee 6
        br_if 0 (;@2;)
        local.get 1
        return
      end
      local.get 1
      local.set 3
      block  ;; label = @2
        local.get 7
        local.get 6
        i32.const -1
        i32.xor
        local.get 1
        i32.const 8
        i32.add
        local.tee 2
        local.get 5
        i32.add
        local.tee 8
        i32.add
        i32.const -65536
        i32.and
        i32.eq
        br_if 0 (;@2;)
        local.get 2
        i32.const 65535
        i32.and
        local.set 6
        block  ;; label = @3
          local.get 0
          i32.const 65271
          i32.gt_u
          br_if 0 (;@3;)
          local.get 7
          local.get 2
          i32.const 8
          i32.shr_u
          i32.const 255
          i32.and
          i32.add
          i32.const 254
          i32.store8
          local.get 1
          i32.const 0
          i32.load offset=1072
          i32.store
          local.get 1
          i32.const 65536
          local.get 6
          i32.sub
          local.tee 6
          i32.store offset=4
          i32.const 0
          local.get 1
          i32.store offset=1072
          call $maybe_repurpose_single_chunk_large_objects_head
          local.get 7
          i32.const 65796
          i32.add
          local.get 5
          local.get 6
          i32.sub
          i32.const -264
          i32.add
          local.tee 6
          i32.store
          local.get 7
          i32.const 65537
          i32.add
          i32.const 255
          i32.store8
          local.get 7
          i32.const 65792
          i32.add
          local.set 3
          local.get 6
          local.get 0
          i32.sub
          i32.const -256
          i32.and
          local.set 6
          br 1 (;@2;)
        end
        local.get 5
        local.get 6
        i32.add
        local.get 0
        local.get 6
        i32.add
        i32.const -1
        i32.add
        i32.const -65536
        i32.and
        i32.sub
        i32.const -65536
        i32.add
        local.set 6
        local.get 1
        local.set 3
      end
      local.get 3
      local.get 3
      i32.load offset=4
      local.get 6
      i32.sub
      i32.store offset=4
      local.get 6
      i32.const 248
      i32.add
      local.set 7
      local.get 8
      local.get 6
      i32.sub
      i32.const 8
      i32.shr_u
      i32.const 255
      i32.and
      local.set 8
      block  ;; label = @2
        loop  ;; label = @3
          local.get 8
          local.set 2
          local.get 7
          local.tee 6
          i32.const -256
          i32.add
          local.set 7
          local.get 6
          i32.const 248
          i32.eq
          br_if 1 (;@2;)
          i32.const 1
          local.set 8
          local.get 2
          i32.eqz
          br_if 0 (;@3;)
        end
      end
      block  ;; label = @2
        local.get 6
        i32.const 248
        i32.eq
        br_if 0 (;@2;)
        local.get 1
        local.get 5
        i32.add
        local.get 7
        i32.sub
        i32.const -65536
        i32.and
        local.tee 6
        local.get 2
        i32.add
        i32.const 254
        i32.store8
        local.get 6
        local.get 2
        i32.const 8
        i32.shl
        i32.add
        local.tee 6
        i32.const 0
        i32.load offset=1072
        i32.store
        local.get 6
        local.get 7
        i32.store offset=4
        i32.const 0
        local.get 6
        i32.store offset=1072
        call $maybe_repurpose_single_chunk_large_objects_head
      end
      local.get 3
      return
    end
    i32.const 0)
  (func $free (type 1) (param i32)
    (local i32 i32)
    block  ;; label = @1
      local.get 0
      i32.eqz
      br_if 0 (;@1;)
      block  ;; label = @2
        local.get 0
        i32.const -65536
        i32.and
        local.get 0
        i32.const 8
        i32.shr_u
        i32.const 255
        i32.and
        i32.or
        local.tee 1
        i32.load8_u
        local.tee 2
        i32.const 255
        i32.ne
        br_if 0 (;@2;)
        local.get 0
        i32.const -8
        i32.add
        local.tee 0
        i32.const 0
        i32.load offset=1072
        i32.store
        i32.const 0
        local.get 0
        i32.store offset=1072
        local.get 1
        i32.const 254
        i32.store8
        i32.const 0
        i32.const 1
        i32.store8 offset=1076
        return
      end
      local.get 0
      local.get 2
      i32.const 2
      i32.shl
      i32.const 1088
      i32.add
      local.tee 2
      i32.load
      i32.store
      local.get 2
      local.get 0
      i32.store
    end)
  (func $maybe_repurpose_single_chunk_large_objects_head (type 2)
    (local i32 i32)
    block  ;; label = @1
      i32.const 0
      i32.load offset=1072
      local.tee 0
      i32.load offset=4
      i32.const 255
      i32.gt_u
      br_if 0 (;@1;)
      local.get 0
      i32.const -65536
      i32.and
      local.tee 1
      local.get 0
      i32.const 8
      i32.shr_u
      i32.const 255
      i32.and
      local.tee 0
      i32.or
      i32.const 9
      i32.store8
      i32.const 0
      i32.const 0
      i32.load offset=1072
      i32.load
      i32.store offset=1072
      local.get 1
      local.get 0
      i32.const 8
      i32.shl
      i32.or
      local.tee 0
      i32.const 0
      i32.load offset=1124
      i32.store
      i32.const 0
      local.get 0
      i32.store offset=1124
    end)
  (table (;0;) 1 1 funcref)
  (global (;0;) (mut i32) (i32.const 66672))
  (export "walloc" (func $walloc))
  (export "wfree" (func $wfree))
  (data (;0;) (i32.const 1024) "\08\00\00\00\10\00\00\00\18\00\00\00 \00\00\00(\00\00\000\00\00\00@\00\00\00P\00\00\00\80\00\00\00\00\01\00\00")
  (data (;1;) (i32.const 1072) "\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00\00"))
