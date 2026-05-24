// -----------------------------------------------
// shader.js — 頂点シェーダー & フラグメントシェーダー
// -----------------------------------------------

export const VERT_SRC = `
  attribute vec2 a_position;   // 頂点座標（クリップ空間 -1〜+1）
  attribute vec2 a_texCoord;   // UV座標（0〜1）
  uniform mat3 u_matrix;       // 移動・スケール行列
  varying vec2 v_texCoord;

  void main() {
    vec3 pos = u_matrix * vec3(a_position, 1.0);
    gl_Position = vec4(pos.xy, 0.0, 1.0);
    v_texCoord = a_texCoord;
  }
`;

export const FRAG_SRC = `
  precision mediump float;
  uniform sampler2D u_texture;
  varying vec2 v_texCoord;

  void main() {
    gl_FragColor = texture2D(u_texture, v_texCoord);
  }
`;
