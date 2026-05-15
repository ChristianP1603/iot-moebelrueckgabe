export const TYP_LABELS = {
  SCHREIBTISCH: "Schreibtisch",
  STUHL: "Stuhl",
  TISCH: "Tisch",
  SCHRANK: "Schrank",
  ROLLCONTAINER: "Rollcontainer",
  REGAL: "Regal",
  LAMPE: "Lampe",
  WHITEBOARD: "Whiteboard",
  SOFA: "Sofa",
  GARDEROBE: "Garderobe",
  SESSEL: "Sessel",
  STEHPULT: "Stehpult",
  KONFERENZTISCH: "Konferenztisch",
  AKTENSCHRANK: "Aktenschrank",
  SIDEBOARD: "Sideboard",
  KOMMODE: "Kommode",
  VITRINE: "Vitrine",
  FLIPCHART: "Flipchart",
  PINNWAND: "Pinnwand",
  BETT: "Bett",
  MATRATZE: "Matratze",
  COUCHTISCH: "Couchtisch",
  SONSTIGES: "Sonstiges",
};

export function typLabel(typ) {
  return TYP_LABELS[typ] || typ;
}
