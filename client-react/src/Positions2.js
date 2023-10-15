import { useState } from "react";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';

import DataTable from './DataTable';
import { renderPositionPopup } from './PositionPopup';
import { getAmount, getPct, getPctChange } from "./tableUtils";

const columns = [
	{
		name: "Ticker",
		align: "l",
		getValue: p => p.symbol,
		render: r => (
			<td key={r.key} className={r.column.align}>
				<a href={`https://client.schwab.com/SymbolRouting.aspx?Symbol=${r.value}`} style={r.record.peRatio < 0 ? { backgroundColor: "#FDD" } : null}>{r.value}</a>
			</td>
		),
		sortDirection: 1
	},
	{
		name: "",
		align: "l",
		getValue: p => p.symbol,
		render: r => (
			<td key={r.key} className={r.column.align}>
				<a href={`https://finance.yahoo.com/quote/${r.record.symbol}`}>
					<img src="https://s.yimg.com/cv/apiv2/default/icons/favicon_y19_32x32_custom.svg" alt="Yahoo" width={14} height={14} />
				</a>
			</td>
		)
	},
	{ name: "#", align: "r", getValue: p => Math.abs(p.quantity), sortDirection: -1, modes: ["view"] },
	getAmount("Price", p => p.price),
	{ ...getAmount("Value", p => p.marketValue), modes: ["view"] },
	getPctChange("Day", p => p.dayChangePct),
	getPctChange("G/L", p => p.gainLossPct),
	{ ...getPct("Actual", p => p.percentOfAccount), modes: ["view"] },
	{ ...getPct("Target", p => p.targetPct), modes: ["view"] },
	{ ...getPct("Ratio", p => getRatio(p), 1), modes: ["view", "trades"] },
	{
		name: "Trade",
		align: "c",
		getValue: p => p.sharesToBuy * p.price,
		render: r => {
			const action = r.record.sharesToBuy > 0 ? "Buy" : "Sell";
			return (
				<td key={r.key} className={r.column.align}>
					<a href={`https://client.schwab.com/Areas/Trade/Allinone/index.aspx?tradeaction=${action}&Symbol=${r.record.symbol}`}>
						<button style={{ minWidth: 55 }} onClick={() => copyClip(Math.abs(r.record.sharesToBuy))}>{`${action} ${Math.abs(r.record.sharesToBuy)}`}</button>
					</a>
				</td>
			);
		},
		modes: ["trades"]
	},
	{
		name: "Sell Calls",
		align: "c",
		getValue: p => p.callsToSell,
		render: r => (<td key={r.key} className={r.column.align}><a href={`https://client.schwab.com/Areas/Trade/Options/Chains/Index.aspx#symbol/${r.record.symbol}`}><button>Sell {r.value}</button></a></td>),
		modes: ["calls"]
	},
	{
		name: "Open",
		align: "c",
		getValue: () => 0,
		render: r => {
			const isOption = r.tableState.mode === "calls";
			const action = r.record.sharesToBuy > 0 ? "Buy" : "Sell";
			const relevantOpenOrders = r.record.openOrders ? r.record.openOrders.filter(o => o.option === isOption) : [];
			const openBuyCount = relevantOpenOrders.filter(o => o.action.startsWith("Buy")).map(o => o.quantity).reduce((tot, cur) => tot + cur, 0);
			const openSellCount = relevantOpenOrders.filter(o => o.action.startsWith("Sell")).map(o => o.quantity).reduce((tot, cur) => tot + cur, 0);
			const openOrderArray = [['B', openBuyCount], ['S', openSellCount]].filter(a => a[1] !== 0);
			const openOrderText = r.tableState.mode === "view"
				? openOrderArray.map(a => a.join(" ")).join(", ")
				: openOrderArray.filter(a => a[0] === action.charAt(0)).map(a => a[1]).join("");
			return (
				<td key={r.key} className={r.column.align}>
					<a href="https://client.schwab.com/Trade/OrderStatus/ViewOrderStatus.aspx?ViewTypeFilter=Open">{openOrderText}</a>
				</td>
			)
		}
	},
	{ ...getAmount("Amount", p => p.sharesToBuy * p.price), sortDirection: 1, modes: ["trades"] },
	{
		name: "",
		align: "l",
		getValue: () => 0,
		render: r => (
			<OverlayTrigger key={r.key} trigger={['hover', 'focus']} placement="right" overlay={renderPositionPopup(r.record)}>
				<td className={r.column.align}><i className="bi bi-info-circle" style={{ marginLeft: 6 }}></i></td>
			</OverlayTrigger>
		)
	}
];

function Positions2(props) {
	const { portfolio } = props;

	const [mode, setMode] = useState("trades");

	let viewPositions = portfolio.positions.filter(p => !p.symbol.includes(" "));
	if (mode === "view") {
		viewPositions = viewPositions.sort((a, b) => b.marketValue - a.marketValue);
	} else if (mode === "trades") {
		// viewPositions = viewPositions.filter(p => p.sharesToBuy).sort((a, b) => (a.sharesToBuy * a.price) - (b.sharesToBuy * b.price));
		viewPositions = viewPositions.filter(p => p.sharesToBuy).sort((a, b) => getRatio(b) - getRatio(a));
	} else if (mode === "calls") {
		viewPositions = viewPositions.filter(p => p.callsToSell).sort((a, b) => b.dayChangePct - a.dayChangePct);
	}

	const visibleColumns = columns.filter(c => !c.modes || c.modes.indexOf(mode) !== -1);

	return (
		<div className="pm-block">
			{renderHeading(portfolio, mode, setMode, viewPositions)}
			<DataTable columns={visibleColumns} records={viewPositions} tableState={{ mode }} />
		</div>
	);
}

function renderHeading(portfolio, mode, setMode, positions) {
	return (
		<div className="pm-heading">
			<div className="pm-title">
				<>
					<input type="radio" name="positionMode" id="mode-view" value="view" checked={mode === "view"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-view" style={{ paddingLeft: 3, marginRight: 10 }}>Positions</label>
				</>
				<>
					<input type="radio" name="positionMode" id="mode-trade-shares" value="trades" checked={mode === "trades"} onChange={(e) => setMode(e.target.value)} />
					<label htmlFor="mode-trade-shares" style={{ paddingLeft: 3, marginRight: 10 }}>Trades</label>
				</>
				{portfolio.optionsEnabled && (
					<>
						<input type="radio" name="positionMode" id="mode-sell-calls" value="calls" checked={mode === "calls"} onChange={(e) => setMode(e.target.value)} />
						<label htmlFor="mode-sell-calls" style={{ paddingLeft: 3, marginRight: 10 }}>Calls to Sell</label>
					</>
				)}
			</div>
			<span>({positions.length})</span>
		</div>
	);
}

function getRatio(position) {
	return position.percentOfAccount && position.targetPct ? (100 * position.percentOfAccount / position.targetPct) : null;
}

function copyClip(text) {
	navigator.clipboard.writeText(text);
}

export default Positions2;
