import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Popover from 'react-bootstrap/Popover';

function Positions(props) {
	const { portfolio } = props;

	const sharePositions = portfolio.positions
		.filter(p => !p.symbol.includes(" "))
		.sort((a, b) => b.marketValue - a.marketValue);

	if (sharePositions.length === 0) {
		return null;
	}

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Positions ({sharePositions.length})</div>
			</div>
			<table>
				<thead>
					<tr>
						<th className="l f">Ticker</th>
						<th>#</th>
						<th>Price</th>
						<th>Value</th>
						<th>Day</th>
						<th>G/L</th>
						<th>Actual</th>
						<th>Target</th>
						<th>Ratio</th>
					</tr>
				</thead>
				<tbody>
					{sharePositions.map(renderRow)}
				</tbody>
			</table>
		</div>
	);
}

function renderRow(position, i) {
	return (
		<tr key={position.symbol}>
			<td className="l f">
				<OverlayTrigger trigger={['hover', 'focus']} placement="right" overlay={renderPopover(position, i)}>
					<a href={`https://finance.yahoo.com/quote/${position.symbol}`} target="_blank" style={position.peRatio < 0 ? { backgroundColor: "#FDD" } : {}}>{position.symbol}</a>
				</OverlayTrigger>
			</td>
			<td>{position.quantity}</td>
			<td>{position.price.toFixed(2)}</td>
			<td>{position.marketValue.toFixed(2)}</td>
			<td style={{ color: position.dayChangePct >= 0 ? "green" : "#C00" }}>{Math.abs(position.dayChangePct).toFixed(2) + "%"}</td>
			<td style={{ color: position.gainLossPct >= 0 ? "green" : "#C00" }}>{Math.abs(position.gainLossPct).toFixed(2) + "%"}</td>
			<td>{position.percentOfAccount.toFixed(2) + "%"}</td>
			<td>{position.targetPct ? position.targetPct.toFixed(2) + "%" : ""}</td>
			<td>{position.targetPct ? (100 * position.percentOfAccount / position.targetPct).toFixed(1) + "%" : ""}</td>
		</tr>
	);
}

function renderPopover(position, i) {
	const low = position['52WeekLow'];
	const high = position['52WeekHigh'];
	const priceRangePct = 100 * (position.price - low) / (high - low);

	return (
		<Popover id={`popover-position-${i}`} style={{ maxWidth: 400 }}>
			<Popover.Header as="h3">{position.description}</Popover.Header>
			<Popover.Body>
				<table>
					<tbody>
						<tr><td className="l">52 week range:</td><td className="l">{`${low} - ${high} (${priceRangePct.toFixed(1)}%)`}</td></tr>
						{position.dividendYield && <tr><td className="l">Dividend yield:</td><td className="l">{`${position.dividendYield}%`}</td></tr>}
						{position.peRatio && <tr><td className="l">P/E ratio:</td><td className="l">{position.peRatio}</td></tr>}
					</tbody>
				</table>
			</Popover.Body>
		</Popover>
	);
}

export default Positions;
